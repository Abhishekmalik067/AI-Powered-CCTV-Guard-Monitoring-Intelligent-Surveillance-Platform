package com.example.ui

import android.app.Application
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.CctvApplication
import com.example.data.CctvCamera
import com.example.data.CctvRepository
import com.example.data.SecurityLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

enum class AuditStep {
    IDLE,
    ANNOUNCEMENT_1,  // "GUARD SAHEB!"
    ANNOUNCEMENT_2,  // "GUARD SAHEB REPORTING KARO!"
    ANNOUNCEMENT_3,  // "CAMERA KE SAMNE AAJAO GUARD SAHEB!"
    WAIT_RESPONSE,   // Waiting 15s for visual/voice response
    SIREN_ACTIVE,    // Alerting with security siren!
    CONFIRMATION,    // Guard responded; asking "SAB THIK HNA WHA PAR GUARD SHABH?"
    FINAL_BLESSING,  // Welcoming response: "THIK HA GUARD SHABH DHYAN RAKHNA WHA PAR"
    PTZ_SWEEP,       // Moving camera left, right, then center
    COMPLETED_ABSENT, // Siren ignored, logging absent, stopping recording, rotation sweep
    COMPLETED_ACTIVE  // Logged active, normal rotation sweep finished
}

class CctvViewModel(
    application: Application,
    private val repository: CctvRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndPrepopulate()
        }
    }

    // Cameras and Logs Flows from DB
    val cameras: StateFlow<List<CctvCamera>> = repository.allCameras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<SecurityLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state variables
    var activeCamera = MutableStateFlow<CctvCamera?>(null)
        private set

    var auditStep = MutableStateFlow(AuditStep.IDLE)
        private set

    var ptzPanAngle = MutableStateFlow(0f) // -90f to +90f
        private set

    var isRecording = MutableStateFlow(false)
        private set

    var countdownTime = MutableStateFlow(0) // seconds remaining
        private set

    var simulatedGuardPresent = MutableStateFlow(false)
        private set

    var selectedIntervalHours = MutableStateFlow(0.5f) // schedule editor

    // Manual Pan Controls
    fun panLeft() {
        val current = ptzPanAngle.value
        if (current > -90f) {
            ptzPanAngle.value = (current - 15f).coerceAtLeast(-90f)
        }
    }

    fun panRight() {
        val current = ptzPanAngle.value
        if (current < 90f) {
            ptzPanAngle.value = (current + 15f).coerceAtMost(90f)
        }
    }

    fun resetPan() {
        ptzPanAngle.value = 0f
    }

    // Audio generator job for siren/beeps
    private var audioJob: Job? = null
    private var auditTimerJob: Job? = null

    // For manual logs
    var inputVehiclePlate = MutableStateFlow("")
    var inputVehicleType = MutableStateFlow("Truck") // Truck, Dumper, Car, Bike
    var inputVehicleInOut = MutableStateFlow("IN")   // IN, OUT
    var inputFurnaceTemp = MutableStateFlow("1550")   // Celsius temp

    fun selectCamera(camera: CctvCamera) {
        if (auditStep.value != AuditStep.IDLE) return // Don't interrupt running workflow
        activeCamera.value = camera
        isRecording.value = camera.isRecording
    }

    // Toggle manual recording
    fun toggleRecording() {
        val cam = activeCamera.value ?: return
        val newState = !isRecording.value
        isRecording.value = newState
        viewModelScope.launch {
            repository.updateCamera(cam.copy(isRecording = newState))
            // Log manually started
            logEvent(
                cameraName = cam.name,
                type = "Recording",
                guardStatus = "N/A",
                details = if (newState) "Recording manually STARTED for ${cam.brand} CVR/NVR." else "Recording manually STOPPED.",
                reportText = "*CVR RECORDING REPORT*\nCamera: ${cam.name}\nBrand: ${cam.brand}\nTime: ${getFormattedTime()}\nStatus: ${if (newState) "RECORDING ON" else "RECORDING OFF"}"
            )
        }
    }

    // Start 2-Way Speech Audit pipeline
    fun startGuardAudit() {
        val cam = activeCamera.value ?: return
        if (auditStep.value != AuditStep.IDLE) return

        // Ensure recording is set to ON during audit process
        isRecording.value = true
        viewModelScope.launch {
            repository.updateCamera(cam.copy(status = "Active Check", isRecording = true))
        }

        auditTimerJob?.cancel()
        auditTimerJob = viewModelScope.launch(Dispatchers.Default) {
            // STEP 1: GUARD SAHEB!
            auditStep.value = AuditStep.ANNOUNCEMENT_1
            playAudioAnnouncement(800f, 1000f, 500) // Walkie talkie call tones
            delay(2500)

            // STEP 2: GUARD SAHEB REPORTING KARO!
            auditStep.value = AuditStep.ANNOUNCEMENT_2
            playAudioAnnouncement(900f, 1100f, 600)
            delay(2500)

            // STEP 3: CAMERA KE SAMNE AAJAO GUARD SAHEB!
            auditStep.value = AuditStep.ANNOUNCEMENT_3
            playAudioAnnouncement(1000f, 1200f, 700)
            delay(2500)

            // STEP 4: Start countdown for Guard visual check (15 seconds)
            auditStep.value = AuditStep.WAIT_RESPONSE
            countdownTime.value = 15
            while (countdownTime.value > 0) {
                delay(1000)
                countdownTime.value -= 1
                // Check if operator marked guard as present (visual simulation hand wave/ok)
                if (simulatedGuardPresent.value) {
                    executeGuardRespondedWorkflow()
                    return@launch
                }
            }

            // No response -> Trigger Siren!
            executeSirenWorkflow()
        }
    }

    // If Operator clicks "Confirm Guard Wave / Say OK"
    fun confirmGuardResponse() {
        simulatedGuardPresent.value = true
        if (auditStep.value == AuditStep.WAIT_RESPONSE || auditStep.value == AuditStep.SIREN_ACTIVE) {
            auditTimerJob?.cancel()
            auditTimerJob = viewModelScope.launch(Dispatchers.Default) {
                executeGuardRespondedWorkflow()
            }
        }
    }

    private suspend fun executeGuardRespondedWorkflow() {
        stopSiren()
        // STEP 5: Operator speech "SAB THIK HNA WHA PAR GUARD SHABH?"
        auditStep.value = AuditStep.CONFIRMATION
        playAudioAnnouncement(700f, 850f, 600)
        delay(3000) // simulated dialogue pause

        // Guard says YES OK in walkie-talkie
        simulatedGuardPresent.value = true

        // STEP 6: Operator speech "THIK HA GUARD SHABH DHYAN RAKHNA WHA PAR"
        auditStep.value = AuditStep.FINAL_BLESSING
        playAudioAnnouncement(750f, 900f, 600)
        delay(3000)

        // STEP 7: PTZ Camera sweep: Left, Right, then back to original position
        executePtzCameraSweep()

        // STEP 8: Wrap up Active Guard Log
        val cam = activeCamera.value
        if (cam != null) {
            val report = """
                *CCTV AUDIT REPORT [ACTIVE]*
                Camera: ${cam.name} (${cam.brand})
                Time: ${getFormattedTime()}
                Verification Status: GUARD ACTIVE & ALERT
                Observation: Guard came in front of camera, verified safety, PTZ rotation scan successful, preset coordinates rest.
                Result: Safe & Secure.
            """.trimIndent()

            logEvent(
                cameraName = cam.name,
                type = "Guard 2-Way",
                guardStatus = "Active",
                details = "2-Way Verification PASSED. Guard responded, waved hand & verified safety. Scanner sweep executed successfully.",
                reportText = report
            )

            repository.updateCamera(cam.copy(status = "Idle", lastCheckTime = System.currentTimeMillis()))
        }

        auditStep.value = AuditStep.IDLE
        simulatedGuardPresent.value = false
    }

    private suspend fun executeSirenWorkflow() {
        auditStep.value = AuditStep.SIREN_ACTIVE
        startSirenAlertWave()

        countdownTime.value = 12 // sirens sound for 12 seconds
        while (countdownTime.value > 0) {
            delay(1000)
            countdownTime.value -= 1
            if (simulatedGuardPresent.value) {
                // Sensed guard late response!
                executeGuardRespondedWorkflow()
                return
            }
        }

        // STILL ABSENT!
        stopSiren()
        auditStep.value = AuditStep.COMPLETED_ABSENT

        // Rotate Cam anyway as requested "rotate the camera and stop the recording if not come"
        executePtzCameraSweep()

        // Turn OFF recording automatically as requested
        isRecording.value = false

        val cam = activeCamera.value
        if (cam != null) {
            val report = """
                *CCTV AUDIT ALARM [GUARD INACTIVE]*
                Camera: ${cam.name} (${cam.brand})
                Time: ${getFormattedTime()}
                Verification Status: GUARD NOT RESPONDING (ABSENT)
                Observation: Automated 2-way audio broadcasted. No response. Security SIREN triggered for 12s. Still absent. 
                Result: ALERT! Recording automatically halted. Area scanned.
            """.trimIndent()

            logEvent(
                cameraName = cam.name,
                type = "Guard 2-Way",
                guardStatus = "No Response",
                details = "2-Way Verification FAILED. Guard absent. Siren triggered, no response. Recording stopped. Visual scanning sweep forced.",
                reportText = report
            )

            repository.updateCamera(
                cam.copy(
                    status = "Idle",
                    isRecording = false,
                    lastCheckTime = System.currentTimeMillis()
                )
            )
        }

        delay(3000)
        auditStep.value = AuditStep.IDLE
        simulatedGuardPresent.value = false
    }

    private suspend fun executePtzCameraSweep() {
        auditStep.value = AuditStep.PTZ_SWEEP
        
        // Pan Left to -90 degrees
        var angle = 0f
        while (angle > -90f) {
            angle -= 10f
            ptzPanAngle.value = angle
            delay(150)
        }
        delay(500)

        // Pan Right to +90 degrees
        while (angle < 90f) {
            angle += 10f
            ptzPanAngle.value = angle
            delay(150)
        }
        delay(500)

        // Return to 0 degrees (Preset Fix Location)
        while (angle > 0f) {
            angle -= 10f
            ptzPanAngle.value = angle
            delay(150)
        }
        ptzPanAngle.value = 0f
        delay(500)
    }

    // Siren Sound Waves synthesis generator!
    private fun startSirenAlertWave() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val numSamples = 44100 * 2 // 2 seconds loop
            val generatedSnd = DoubleArray(numSamples)
            val soundData = ShortArray(numSamples)

            // Dynamic frequency sweeps (Siren chirp sound)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Frequency modulated between 600Hz and 1300Hz
                val dynamicFreq = 950.0 + 350.0 * sin(2.0 * Math.PI * 1.5 * t)
                generatedSnd[i] = sin(2.0 * Math.PI * dynamicFreq * t)
                soundData[i] = (generatedSnd[i] * 32767).toInt().toShort()
            }

            try {
                val audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STREAM
                )
                audioTrack.play()
                while (isActive && auditStep.value == AuditStep.SIREN_ACTIVE) {
                    audioTrack.write(soundData, 0, soundData.size)
                }
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopSiren() {
        audioJob?.cancel()
        audioJob = null
    }

    // Sound walkie-talkie audio feed beep
    private fun playAudioAnnouncement(f1: Float, f2: Float, durationMs: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            val sampleRate = 44100
            val bufferSize = sampleRate * durationMs / 1000
            val soundData = ShortArray(bufferSize)
            for (i in 0 until bufferSize) {
                val t = i.toDouble() / sampleRate
                // Dual frequency chirp for synthetic walkie talkie speech start tone
                val wave = 0.5 * sin(2.0 * Math.PI * f1 * t) + 0.5 * sin(2.0 * Math.PI * f2 * t)
                soundData[i] = (wave * 32767).toInt().toShort()
            }
            try {
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize * 2,
                    AudioTrack.MODE_STATIC
                )
                track.write(soundData, 0, soundData.size)
                track.play()
                delay(durationMs.toLong() + 100)
                track.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Manual Event simulation: Log Gate Vehicle entry
    fun simulateVehicleLog() {
        val cam = activeCamera.value ?: return
        val plate = inputVehiclePlate.value.ifBlank { "HR-26-DJ-${(1000..9999).random()}" }.uppercase()
        val type = inputVehicleType.value
        val dir = inputVehicleInOut.value

        val timestampStr = getFormattedTime()
        val report = """
            *VEHICLE LOG REPORT [GATE ENTRY]*
            Camera: ${cam.name}
            Time: $timestampStr
            Vehicle: $type
            License Plate: $plate
            Direction: $dir
            Status: Captured screen frame successfully and indexed.
        """.trimIndent()

        viewModelScope.launch {
            logEvent(
                cameraName = cam.name,
                type = "Vehicle Entry",
                guardStatus = "N/A",
                details = "Vehicle [$type] with registration plate [$plate] passed gate going $dir. Snapshot logged.",
                reportText = report
            )
            inputVehiclePlate.value = "" // clear input
        }
    }

    // Manual Event simulation: Log furnace on/off timing (Mining industry)
    fun simulateFurnaceLog(isOn: Boolean) {
        val cam = activeCamera.value ?: return
        val temp = inputFurnaceTemp.value.ifBlank { "1550" }
        val timestampStr = getFormattedTime()

        val detailsStr = if (isOn) {
            "Induction Furnace powered ON. Pre-heating initialized. Target Temp: ${temp}°C."
        } else {
            "Induction Furnace powered OFF. Cooling profile logged. Current temp: ${temp}°C."
        }

        val report = """
            *FURNACE LOG [MINING INDUSTRIAL]*
            Camera: ${cam.name}
            Timing: $timestampStr
            Command: ${if (isOn) "POWER ON" else "POWER OFF"}
            Industrial Temp: ${temp}°C
            Status: Logged to furnace operations log.
        """.trimIndent()

        viewModelScope.launch {
            logEvent(
                cameraName = cam.name,
                type = "Furnace State",
                guardStatus = "N/A",
                details = detailsStr,
                reportText = report
            )
        }
    }

    // Manual Event simulation: Illegal parking detection
    fun simulateIllegalParking() {
        val cam = activeCamera.value ?: return
        val timestampStr = getFormattedTime()
        val plate = "UP-16-AB-${(1000..9999).random()}"
        val report = """
            *PARKING VIOLATION REPORT*
            Camera: ${cam.name}
            Time: $timestampStr
            Violation: Unapproved vehicle parked in front of shop gate/road.
            Vehicle Plate: $plate
            Security Action: Audio alert broadcasted.
        """.trimIndent()

        viewModelScope.launch {
            logEvent(
                cameraName = cam.name,
                type = "Illegal Parking",
                guardStatus = "N/A",
                details = "Alert: Vehicle ($plate) blocking shop gate / main access road. Audio notice played.",
                reportText = report
            )
            playAudioAnnouncement(1200f, 1500f, 400)
        }
    }

    // Manual Event simulation: Sleeping guard check
    fun simulateGuardSleepingAlert() {
        val cam = activeCamera.value ?: return
        val timestampStr = getFormattedTime()
        val report = """
            *SECURITY BREACH ALARM [GUARD SLEEPING]*
            Camera: ${cam.name}
            Time: $timestampStr
            Violation: AI analysis detected guard closing eyes / inactive for >5 mins.
            Result: Triggered command deck alert and local speaker chime.
        """.trimIndent()

        viewModelScope.launch {
            logEvent(
                cameraName = cam.name,
                type = "Guard 2-Way",
                guardStatus = "Sleeping",
                details = "AI Warning: Guard detected Sleeping on duty. Automatic alert chime sounded.",
                reportText = report
            )
            playAudioAnnouncement(300f, 300f, 1000) // dull warning alarm buzz
        }
    }

    // Manual Event simulation: Guard playing phone
    fun simulateGuardPhoneAlert() {
        val cam = activeCamera.value ?: return
        val timestampStr = getFormattedTime()
        val report = """
            *SECURITY AUDIT REMINDER [PHONE DISTRACTION]*
            Camera: ${cam.name}
            Time: $timestampStr
            Violation: AI scanning detected guard using mobile phone for extended period.
            Result: Intercom alert dispatched.
        """.trimIndent()

        viewModelScope.launch {
            logEvent(
                cameraName = cam.name,
                type = "Guard 2-Way",
                guardStatus = "Using Mobile",
                details = "AI Alert: Guard using smartphone. Dispatched speech warning to active handset.",
                reportText = report
            )
            playAudioAnnouncement(500f, 700f, 500)
        }
    }

    // Helper: Add camera dynamically
    fun addCamera(name: String, brand: String, location: String, intervalHours: Float, isVehicle: Boolean, isFurnace: Boolean, isParking: Boolean) {
        viewModelScope.launch {
            val newCam = CctvCamera(
                name = name,
                brand = brand,
                location = location,
                intervalHours = intervalHours,
                isRecording = true,
                status = "Idle",
                isVehicleMonitor = isVehicle,
                isFurnaceMonitor = isFurnace,
                parkingMonitor = isParking
            )
            repository.insertCamera(newCam)
            
            logEvent(
                cameraName = name,
                type = "System",
                guardStatus = "N/A",
                details = "New $brand camera [$name] installed with auto ${intervalHours}h verification schedules.",
                reportText = "*CAMERA CONFIGURED*\nCamera: $name\nAuto Check Interval: ${intervalHours} Hrs\nBrand: $brand\nLocation: $location"
            )
        }
    }

    // Save camera schedule update
    fun updateCameraInterval(hours: Float) {
        val cam = activeCamera.value ?: return
        viewModelScope.launch {
            val updated = cam.copy(intervalHours = hours)
            repository.updateCamera(updated)
            activeCamera.value = updated
            selectedIntervalHours.value = hours

            logEvent(
                cameraName = cam.name,
                type = "System",
                guardStatus = "N/A",
                details = "Adjusted check-in frequency to ${hours} hours.",
                reportText = "*FREQUENCY ADJUSTED*\nCamera: ${cam.name}\nNew Rule: Run 2-way speech every ${hours} hrs."
            )
        }
    }

    // Helper: Logging events
    private suspend fun logEvent(
        cameraName: String,
        type: String,
        guardStatus: String,
        details: String,
        reportText: String
    ) {
        val log = SecurityLog(
            timestamp = System.currentTimeMillis(),
            cameraName = cameraName,
            type = type,
            guardStatus = guardStatus,
            details = details,
            reportText = reportText
        )
        repository.insertLog(log)
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllLogs()
        }
    }

    private fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onCleared() {
        super.onCleared()
        audioJob?.cancel()
        auditTimerJob?.cancel()
    }
}

class CctvViewModelFactory(
    private val application: Application,
    private val repository: CctvRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CctvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CctvViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
