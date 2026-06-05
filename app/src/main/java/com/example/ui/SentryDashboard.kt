package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CctvCamera
import com.example.data.SecurityLog
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

// Color tokens for CCTV Operator Automator Dark Ops Theme
val CyberDarkBg = Color(0xFF080C10)
val CyberDarkSurface = Color(0xFF0E141B)
val CyberDarkCard = Color(0xFF161F29)
val CyberGreen = Color(0xFF00FFB0)
val CyberGreenDim = Color(0xFF004D36)
val CyberAmber = Color(0xFFFF9F0A)
val CyberRed = Color(0xFFFF453A)
val CyberIce = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentryDashboard(viewModel: CctvViewModel) {
    val cameras by viewModel.cameras.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val activeCam by viewModel.activeCamera.collectAsStateWithLifecycle()
    val currentAuditStep by viewModel.auditStep.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Sentry") } // Sentry, Logs, Directory
    var showAddCameraDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Set default active camera if none is active on initial load
    LaunchedEffect(cameras) {
        if (activeCam == null && cameras.isNotEmpty()) {
            viewModel.selectCamera(cameras.first())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Core",
                            tint = CyberGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AUTO-GUARD SENTRY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "CCTV Operator Automation Console [Online]",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberGreenDim)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DECK AUTO-MODE ACTIVE",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberDarkSurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberDarkSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = activeTab == "Sentry",
                    onClick = { activeTab = "Sentry" },
                    icon = { Icon(Icons.Default.Videocam, "Live Sentry") },
                    label = { Text("Live Sentry", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberGreen,
                        selectedTextColor = CyberGreen,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = CyberDarkCard
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Logs",
                    onClick = { activeTab = "Logs" },
                    icon = { Icon(Icons.Default.Assessment, "Reports Sheet") },
                    label = { Text("Reports Sheet", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberGreen,
                        selectedTextColor = CyberGreen,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = CyberDarkCard
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "Directory",
                    onClick = { activeTab = "Directory" },
                    icon = { Icon(Icons.Default.Schedule, "Directory & Timers") },
                    label = { Text("Cameras Timer", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberGreen,
                        selectedTextColor = CyberGreen,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = CyberDarkCard
                    )
                )
            }
        },
        containerColor = CyberDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(CyberDarkBg)
        ) {
            when (activeTab) {
                "Sentry" -> SentryScreen(viewModel, cameras)
                "Logs" -> LogsScreen(viewModel, logs)
                "Directory" -> DirectoryScreen(viewModel, cameras) { showAddCameraDialog = true }
            }
        }
    }

    if (showAddCameraDialog) {
        AddCameraDialog(
            onDismiss = { showAddCameraDialog = false },
            onConfirm = { name, brand, loc, iv, isV, isF, isP ->
                viewModel.addCamera(name, brand, loc, iv, isV, isF, isP)
                showAddCameraDialog = false
                Toast.makeText(context, "Added camera: $name scheduled!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// -------------------- SCREEN 1: LIVE SURVEY MONITOR --------------------
@Composable
fun SentryScreen(viewModel: CctvViewModel, cameras: List<CctvCamera>) {
    val activeCam by viewModel.activeCamera.collectAsStateWithLifecycle()
    val isRec by viewModel.isRecording.collectAsStateWithLifecycle()
    val currentStep by viewModel.auditStep.collectAsStateWithLifecycle()
    val panAngle by viewModel.ptzPanAngle.collectAsStateWithLifecycle()
    val countdown by viewModel.countdownTime.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Horizontal Camera Strip Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cameras.forEach { camera ->
                val isSelected = activeCam?.id == camera.id
                val borderBrush = if (isSelected) {
                    Brush.sweepGradient(listOf(CyberGreen, CyberIce))
                } else {
                    Brush.linearGradient(listOf(Color.DarkGray, Color.DarkGray))
                }
                
                Box(
                    modifier = Modifier
                        .width(135.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberDarkCard else CyberDarkSurface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            brush = borderBrush,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectCamera(camera) }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = camera.brand.uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyberGreen else Color.Gray
                                )
                            )
                            if (camera.isRecording) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(CyberRed)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = camera.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every ${camera.intervalHours}h",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        if (activeCam == null) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                    .background(CyberDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideocamOff,
                        contentDescription = "No Camera",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NO CAMERAS LINKED",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            return
        }

        val camera = activeCam!!

        // LIVE FEED CANVAS VIEW (Simulated viewport with pan angle feedback)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, if (currentStep != AuditStep.IDLE) CyberGreen else Color.DarkGray, RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            // Simulated outdoor feed drawing
            LiveCctvCanvas(
                cameraName = camera.name,
                brand = camera.brand,
                panAngle = panAngle,
                isRecording = isRec,
                currentStep = currentStep
            )

            // Status Badge Overlay
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isRec) CyberRed.copy(alpha = 0.2f) else Color.DarkGray)
                        .border(1.dp, if (isRec) CyberRed else Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isRec) {
                            BlinkingRedDot()
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = if (isRec) "LIVE CVR ON" else "CVR STOPPED",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (currentStep != AuditStep.IDLE) CyberGreen.copy(alpha = 0.2f) else Color.DarkGray)
                        .border(1.dp, if (currentStep != AuditStep.IDLE) CyberGreen else Color.LightGray, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PTZ: ${panAngle.toInt()}°",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Watermark brand Overlay
            Text(
                text = "${camera.brand.uppercase()} CLIENT CONTROL",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                color = Color.White.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )

            // Security watermark Grid layout
            Text(
                text = "SECURE AUTO-COMMS PRESENCE AUDIT",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                color = CyberGreen.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
            
            // Notification overlay (siren etc)
            if (currentStep == AuditStep.SIREN_ACTIVE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CyberRed.copy(alpha = 0.15f))
                        .border(4.dp, CyberRed, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Siren Alert",
                            tint = CyberRed,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "SIREN ALERT DEPLOYED!",
                            color = CyberRed,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Blasting audio speakers at station...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MANUAL CAMERA PHYSICAL CONTROLS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = BorderStroke(1.dp, CyberDarkCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Manual Record Toggle
                Column {
                    Text(
                        text = "CVR RECORDING LOCK",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.toggleRecording()
                            Toast.makeText(context, if (!isRec) "NVR Recording LOCK COMMITTED!" else "NVR Recording RECOG-HALTED!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRec) CyberRed else CyberDarkCard
                        ),
                        border = BorderStroke(1.dp, if (isRec) CyberRed else CyberGreen),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isRec) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Rec Lock",
                            tint = if (isRec) Color.White else CyberGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRec) "RECORDING ON" else "START CVR",
                            color = if (isRec) Color.White else CyberGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .width(1.dp)
                        .background(Color.DarkGray)
                )

                // Right: PTZ Pan Joystick Simulation
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PTZ MANUAL PAN CONTROLS",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { viewModel.panLeft() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCard),
                            border = BorderStroke(1.dp, CyberIce),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Pan Left", tint = CyberIce, modifier = Modifier.size(18.dp))
                            Text("LG-L", color = CyberIce, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.resetPan() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCard),
                            border = BorderStroke(1.dp, Color.LightGray),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, "Center Focus", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("0°", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { viewModel.panRight() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCard),
                            border = BorderStroke(1.dp, CyberIce),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("LG-R", color = CyberIce, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.ChevronRight, "Pan Right", tint = CyberIce, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CENTRAL AUTOMATION PLAYGROUND & CONTROLS
        Text(
            text = "GUARD verification check-in (two-way automation)".uppercase(),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberGreen
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = BorderStroke(1.dp, CyberDarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Main Play/Trigger Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Guard 2-Way Speech Routine",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Fires Urdu/Hindi speaker statements systematically.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }

                    if (currentStep == AuditStep.IDLE) {
                        Button(
                            onClick = { viewModel.startGuardAudit() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, "Trigger Audio", tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN CHECK", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { /* Disable click when running to protect flow */ },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "CHECK ONGOING",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pipeline workflow progress drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberDarkCard)
                        .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "SPEAKER BROADCAST AUTOMATION STREAM",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Render speech step
                        val speechBubbleText = when (currentStep) {
                            AuditStep.IDLE -> "[SYSTEM STANDBY] Click Run Check to start automated sequence."
                            AuditStep.ANNOUNCEMENT_1 -> "📣 Broadcasting audio: \"GUARD SAHEB!\""
                            AuditStep.ANNOUNCEMENT_2 -> "📣 Broadcasting audio: \"GUARD SAHEB REPORTING KARO!\""
                            AuditStep.ANNOUNCEMENT_3 -> "📣 Broadcasting: \"CAMERA KE SAMNE AAJAO GUARD SAHEB!\""
                            AuditStep.WAIT_RESPONSE -> "⏳ Expecting visual/verbal feedback... ($countdown Secs left)"
                            AuditStep.SIREN_ACTIVE -> "🚨 SIREN SOUND BLOWING! Guard did not report to mic..."
                            AuditStep.CONFIRMATION -> "📣 Speech: \"SAB THIK HAIN NA VAHAN PAR GUARD SAHEB?\""
                            AuditStep.FINAL_BLESSING -> "📣 Speech: \"THIK HAI GUARD SAHEB DHYAN RAKHNA WHA PAR.\""
                            AuditStep.PTZ_SWEEP -> "🔄 PTZ Scanner: Initiating automatic 180° visual area sweep..."
                            AuditStep.COMPLETED_ABSENT -> "❌ FAILURE: Guard absent. Recorded halted, alert captured."
                            AuditStep.COMPLETED_ACTIVE -> "✓ SUCCESS: Sentry audit successfully delivered to client log."
                        }

                        val textColor = when (currentStep) {
                            AuditStep.IDLE -> Color.LightGray
                            AuditStep.WAIT_RESPONSE -> CyberIce
                            AuditStep.SIREN_ACTIVE, AuditStep.COMPLETED_ABSENT -> CyberRed
                            AuditStep.FINAL_BLESSING, AuditStep.COMPLETED_ACTIVE -> CyberGreen
                            else -> CyberAmber
                        }

                        Text(
                            text = speechBubbleText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        )

                        if (currentStep != AuditStep.IDLE) {
                            Spacer(modifier = Modifier.height(12.dp))
                            // Progress feedback
                            LinearProgressIndicator(
                                progress = {
                                    when (currentStep) {
                                        AuditStep.ANNOUNCEMENT_1 -> 0.1f
                                        AuditStep.ANNOUNCEMENT_2 -> 0.2f
                                        AuditStep.ANNOUNCEMENT_3 -> 0.3f
                                        AuditStep.WAIT_RESPONSE -> 0.45f
                                        AuditStep.SIREN_ACTIVE -> 0.6f
                                        AuditStep.CONFIRMATION -> 0.75f
                                        AuditStep.FINAL_BLESSING -> 0.85f
                                        AuditStep.PTZ_SWEEP -> 0.95f
                                        else -> 1.0f
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = CyberGreen,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }

                if (currentStep == AuditStep.WAIT_RESPONSE || currentStep == AuditStep.SIREN_ACTIVE) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "OPERATOR DESK ACTION DESIRED:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.confirmGuardResponse() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, "Active", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guard Came & Waved / Said OK", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INDUSTRIAL INTEGRATIONS PANELS (VEHICLE, FURNACE, SAFETY MONITORS)
        Text(
            text = "INDUSTRIAL PLANTS & SHIFTS INTEGRATIONS",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace,
                color = CyberIce
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 1. Gate Vehicular Tracking Block
        if (camera.isVehicleMonitor) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberIce.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, "Gate", tint = CyberIce, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Main Gate In/Out Vehicle Capture",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Every entry/exit needs screenshot verification logging.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vehicle type selector
                        var expandedType by remember { mutableStateOf(false) }
                        val types = listOf("Truck", "Dumper", "Tanker", "Car", "Cargo")
                        val selectedType by viewModel.inputVehicleType.collectAsStateWithLifecycle()

                        Box {
                            OutlinedButton(
                                onClick = { expandedType = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(selectedType, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Icon(Icons.Default.ArrowDropDown, "down", modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false },
                                modifier = Modifier.background(CyberDarkSurface)
                            ) {
                                types.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t, color = Color.White, fontFamily = FontFamily.Monospace) },
                                        onClick = {
                                            viewModel.inputVehicleType.value = t
                                            expandedType = false
                                        }
                                    )
                                }
                            }
                        }

                        // IN/OUT Toggle
                        val inOut by viewModel.inputVehicleInOut.collectAsStateWithLifecycle()
                        Box {
                            OutlinedButton(
                                onClick = {
                                    viewModel.inputVehicleInOut.value = if (inOut == "IN") "OUT" else "IN"
                                },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (inOut == "IN") CyberGreen else CyberAmber
                                ),
                                border = BorderStroke(1.dp, if (inOut == "IN") CyberGreen else CyberAmber),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(inOut, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Plate typing
                        val plate by viewModel.inputVehiclePlate.collectAsStateWithLifecycle()
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { viewModel.inputVehiclePlate.value = it },
                            placeholder = { Text("HR-26-DJ-9090", fontSize = 10.sp, color = Color.Gray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberIce,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.simulateVehicleLog()
                            Toast.makeText(context, "Captured screen and logged to database!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberIce),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, "Snap", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TAKE SNAPSHOT & INDEX ENTRY", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Mining Industry Furnace Timing Block
        if (camera.isFurnaceMonitor) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, "Furnace", tint = CyberAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mining Furnace Clocking Sheet",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Register pre-heating furnace ON/OFF operational timestamps to central Excel logs.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Temp (°C):",
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        val temp by viewModel.inputFurnaceTemp.collectAsStateWithLifecycle()
                        OutlinedTextField(
                            value = temp,
                            onValueChange = { viewModel.inputFurnaceTemp.value = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberAmber,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .width(90.dp)
                                .height(46.dp),
                            shape = RoundedCornerShape(4.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                viewModel.simulateFurnaceLog(true)
                                Toast.makeText(context, "Furnace POWER ON logged!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("POWER ON", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateFurnaceLog(false)
                                Toast.makeText(context, "Furnace POWER OFF logged!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("POWER OFF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. Safety Violations and AI Check Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
            border = BorderStroke(1.dp, CyberDarkCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AI Smart Guard Behavior & Obstruction Alarms",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Operator desk simulation triggers for safety exceptions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sleeping check trigger
                    OutlinedButton(
                        onClick = {
                            viewModel.simulateGuardSleepingAlert()
                            Toast.makeText(context, "AI SLEEPING ALERT generated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, "Sleep", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detect Sleep", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mobile usage check trigger
                    OutlinedButton(
                        onClick = {
                            viewModel.simulateGuardPhoneAlert()
                            Toast.makeText(context, "AI SMARTPHONE ALERT generated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberAmber),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.PhoneAndroid, "Phone", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detect Phone", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Parking violation
                    OutlinedButton(
                        onClick = {
                            viewModel.simulateIllegalParking()
                            Toast.makeText(context, "Gate Parking Obstructed log recorded!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, CyberIce.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberIce),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, "Parking", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Block Gate/Road", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE ALERTS QUEUE PANEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Alerts Queue (Database Feed)".uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberGreen
                )
            )
            Text(
                text = "Live updates",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val recentLogs by viewModel.logs.collectAsStateWithLifecycle()
        if (recentLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberDarkSurface)
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO ALERTS RECORDED YET. CLICK TEST BUTTONS.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            recentLogs.take(3).forEach { log ->
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                val alertColor = when (log.guardStatus) {
                    "Active" -> CyberGreen
                    "No Response", "Sleeping" -> CyberRed
                    "Using Mobile" -> CyberAmber
                    else -> CyberIce
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = BorderStroke(1.dp, alertColor.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(alertColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log.type.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = alertColor
                                )
                            }
                            Text(
                                text = timeStr,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.details,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

// -------------------- DETAILED CANVAS DRAWINGS FOR CCTV VIDEO FEED --------------------
@Composable
fun LiveCctvCanvas(
    cameraName: String,
    brand: String,
    panAngle: Float,
    isRecording: Boolean,
    currentStep: AuditStep
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarScanning")
    val radarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScan"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
    ) {
        val width = size.width
        val height = size.height

        // Background color
        drawRect(Color(0xFF03060A))

        // Center visual crosshairs
        val gridColor = Color(0xFF1D2933)
        // Horizontal lines
        drawLine(gridColor, Offset(0f, height * 0.25f), Offset(width, height * 0.25f), 1f)
        drawLine(gridColor, Offset(0f, height * 0.5f), Offset(width, height * 0.5f), 1f)
        drawLine(gridColor, Offset(0f, height * 0.75f), Offset(width, height * 0.75f), 1f)
        // Vertical lines
        drawLine(gridColor, Offset(width * 0.25f, 0f), Offset(width * 0.25f, height), 1f)
        drawLine(gridColor, Offset(width * 0.5f, 0f), Offset(width * 0.5f, height), 1f)
        drawLine(gridColor, Offset(width * 0.75f, 0f), Offset(width * 0.75f, height), 1f)

        // Draw simulated CCTV environment objects based on current angle shift
        // Guard house
        val guardHouseX = (width * 0.5f) - (panAngle * 2.5f)
        drawRect(
            color = Color(0xFF243447),
            topLeft = Offset(guardHouseX - 40f, height * 0.4f),
            size = androidx.compose.ui.geometry.Size(80f, 60f)
        )
        // Gate blocking pole
        drawLine(
            color = CyberAmber,
            start = Offset(guardHouseX - 60f, height * 0.5f),
            end = Offset(guardHouseX + 10f, height * 0.5f),
            strokeWidth = 6f
        )

        // Draw Guard representation standing near gate
        val guardX = (width * 0.45f) - (panAngle * 2.5f)
        val hasActiveGuard = currentStep == AuditStep.CONFIRMATION || currentStep == AuditStep.FINAL_BLESSING || currentStep == AuditStep.COMPLETED_ACTIVE
        
        if (hasActiveGuard || (currentStep == AuditStep.WAIT_RESPONSE && radarPhase > 0.4f)) {
            // Guard body
            drawCircle(Color(0xFF27AE60), 12f, Offset(guardX, height * 0.45f)) // hat
            drawRect(
                color = Color(0xFF1B4F72),
                topLeft = Offset(guardX - 10f, height * 0.45f + 12f),
                size = androidx.compose.ui.geometry.Size(20f, 30f)
            ) // uniform body
            
            // Waving Hand representation
            val waveY = if (radarPhase > 0.5f) height * 0.45f else height * 0.45f + 10f
            drawLine(
                color = Color(0xFFFBEEE6),
                start = Offset(guardX + 10f, height * 0.45f + 16f),
                end = Offset(guardX + 18f, waveY),
                strokeWidth = 4f
            )
        } else if (currentStep == AuditStep.SIREN_ACTIVE) {
            // Empty body showing absent
        } else {
            // Dim dummy outline of guard
            drawCircle(Color.DarkGray, 10f, Offset(guardX, height * 0.45f))
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(guardX - 8f, height * 0.45f + 10f),
                size = androidx.compose.ui.geometry.Size(16f, 25f)
            )
        }

        // Draw camera scanning beam overlay
        val scanY = height * radarPhase
        drawLine(
            color = if (currentStep == AuditStep.SIREN_ACTIVE) CyberRed.copy(alpha = 0.6f) else CyberGreen.copy(alpha = 0.5f),
            start = Offset(0f, scanY),
            end = Offset(width, scanY),
            strokeWidth = 2f
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, if (currentStep == AuditStep.SIREN_ACTIVE) CyberRed.copy(alpha = 0.15f) else CyberGreen.copy(alpha = 0.1f), Color.Transparent),
                startY = scanY - 30f,
                endY = scanY + 10f
            ),
            topLeft = Offset(0f, scanY - 30f),
            size = androidx.compose.ui.geometry.Size(width, 40f)
        )

        // Draw corner brackets defining focus zone
        val bracketLen = 24.dp.toPx()
        val strokeW = 4.dp.toPx()
        val bracketBrush = if (isRecording) CyberRed else CyberGreen
        
        // Top Left
        drawLine(bracketBrush, Offset(16f, 16f), Offset(16f + bracketLen, 16f), strokeW)
        drawLine(bracketBrush, Offset(16f, 16f), Offset(16f, 16f + bracketLen), strokeW)
        // Top Right
        drawLine(bracketBrush, Offset(width - 16f, 16f), Offset(width - 16f - bracketLen, 16f), strokeW)
        drawLine(bracketBrush, Offset(width - 16f, 16f), Offset(width - 16f, 16f + bracketLen), strokeW)
        // Bottom Left
        drawLine(bracketBrush, Offset(16f, height - 16f), Offset(16f + bracketLen, height - 16f), strokeW)
        drawLine(bracketBrush, Offset(16f, height - 16f), Offset(16f, height - 16f - bracketLen), strokeW)
        // Bottom Right
        drawLine(bracketBrush, Offset(width - 16f, height - 16f), Offset(width - 16f - bracketLen, height - 16f), strokeW)
        drawLine(bracketBrush, Offset(width - 16f, height - 16f), Offset(width - 16f, height - 16f - bracketLen), strokeW)
    }
}

@Composable
fun BlinkingRedDot() {
    val transition = rememberInfiniteTransition(label = "BlinkingDot")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkRed"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(CyberRed.copy(alpha = alpha))
    )
}

// -------------------- SCREEN 2: SECURITY EXCEL LOGS SHEET --------------------
@Composable
fun LogsScreen(viewModel: CctvViewModel, logs: List<SecurityLog>) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AUTO-GENERATED OPERATIONS SHEET",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Persisted local auditing entries & safety checks.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // ClearLogs Buttons
                IconButton(
                    onClick = {
                        viewModel.clearLogs()
                        Toast.makeText(context, "Cleared logs history", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.Delete, "Clear", tint = CyberRed)
                }

                // CSV Export Trigger
                Button(
                    onClick = {
                        val csvBuilder = StringBuilder()
                        csvBuilder.append("Timestamp,Camera,Type,GuardStatus,Details\n")
                        logs.forEach { l ->
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(l.timestamp))
                            csvBuilder.append("\"${timeStr}\",\"${l.cameraName}\",\"${l.type}\",\"${l.guardStatus}\",\"${l.details}\"\n")
                        }
                        clipboardManager.setText(AnnotatedString(csvBuilder.toString()))
                        Toast.makeText(context, "CSV exported to Clipboard! (Perfect for Excel pasting)", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Share, "Share XML CSV", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EXCEL EXPORT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                    .background(CyberDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assessment, "Empty", tint = Color.DarkGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("NO SECURITY PROTOCOL LOGS", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text("Run verification audits to compile automatic telemetry.", color = Color.DarkGray, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
            return
        }

        // Logs table
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(logs) { log ->
                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                
                val headerColor = when (log.guardStatus) {
                    "Active" -> CyberGreen
                    "No Response", "Sleeping" -> CyberRed
                    "Using Mobile" -> CyberAmber
                    else -> CyberIce
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(headerColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.type.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = headerColor
                                )
                            }
                            Text(
                                text = timeStr,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = log.cameraName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = log.details,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expandable / copyable report drawer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberDarkCard)
                                .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "CLIENT COMPLIANCE REPORT TEXT:",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.reportText,
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(log.reportText))
                                        Toast.makeText(context, "Copied formatted report to Whatsapp!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCard),
                                    border = BorderStroke(1.dp, CyberGreen),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, "copy", tint = CyberGreen, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Copy", color = CyberGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------- SCREEN 3: CAMERA SECTOR & SCHEDULER DIRECTORY --------------------
@Composable
fun DirectoryScreen(
    viewModel: CctvViewModel,
    cameras: List<CctvCamera>,
    onTriggerAddCamera: () -> Unit
) {
    val activeCam by viewModel.activeCamera.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAMERA TELEMETRY SCHEDULES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Verify/adjust intervals for auto checks here.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onTriggerAddCamera,
                colors = ButtonDefaults.buttonColors(containerColor = CyberIce),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Add, "Add Cam", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ADD CAMERA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected camera configurations settings
        if (activeCam != null) {
            val cam = activeCam!!
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberDarkSurface),
                border = BorderStroke(1.dp, CyberIce)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MODIFY SCHEDULE FOR: ${cam.name.uppercase()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CyberIce,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specify interval for automated security prompts and checks.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interval selector options row
                    val intervals = listOf(0.5f, 1.0f, 2.0f, 3.0f)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervals.forEach { h ->
                            val isChosen = cam.intervalHours == h
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) CyberIce else CyberDarkCard)
                                    .border(1.dp, if (isChosen) CyberIce else Color.Gray, RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewModel.updateCameraInterval(h)
                                        Toast.makeText(context, "Schedules updated to run every $h Hrs!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (h == 0.5f) "30 Min" else "${h.toInt()} Hour",
                                    color = if (isChosen) Color.Black else Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val freqText = if (cam.intervalHours == 0.5f) "30 minutes" else "${cam.intervalHours.toInt()} hour(s)"
                    Text(
                        text = "*Client Verification rule will activate 2-way audio checklist every $freqText*.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fully styled Camera list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cameras) { camera ->
                val isActiveSelected = activeCam?.id == camera.id
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberDarkSurface)
                        .border(
                            1.dp,
                            if (isActiveSelected) CyberGreen else Color.DarkGray,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectCamera(camera) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (camera.isRecording) CyberRed else Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Preset ${camera.brand.uppercase()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberGreen
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = camera.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Loc: ${camera.location}",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberDarkCard)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Int: ${if (camera.intervalHours == 0.5f) "30m" else "${camera.intervalHours.toInt()}h"}",
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Features indicators row
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (camera.isVehicleMonitor) {
                                    Icon(Icons.Default.LocalShipping, "Gate Mode", tint = CyberIce, modifier = Modifier.size(14.dp))
                                }
                                if (camera.isFurnaceMonitor) {
                                    Icon(Icons.Default.LocalFireDepartment, "Furnace Mode", tint = CyberAmber, modifier = Modifier.size(14.dp))
                                }
                                if (camera.parkingMonitor) {
                                    Icon(Icons.Default.ReportProblem, "Area Block Flag", tint = CyberRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Add Custom camera dialog (Material 3 style)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, brand: String, location: String, interval: Float, isVehicle: Boolean, isFurnace: Boolean, isParking: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Dahua") }
    var location by remember { mutableStateOf("") }
    var intervalHours by remember { mutableStateOf(1.0f) }

    var isVehicle by remember { mutableStateOf(false) }
    var isFurnace by remember { mutableStateOf(false) }
    var isParking by remember { mutableStateOf(false) }

    val brandsList = listOf("Dahua", "CP Plus", "Hikvision", "Hanwha", "XMEYE", "EseeCloud")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "CONFIGURE NEW CAM SLOT",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = CyberGreen
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Camera Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Brand Selector Dropdown
                var brandExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { brandExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Brand: $brand", fontFamily = FontFamily.Monospace)
                    }
                    DropdownMenu(
                        expanded = brandExpanded,
                        onDismissRequest = { brandExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberDarkSurface)
                    ) {
                        brandsList.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b, color = Color.White, fontFamily = FontFamily.Monospace) },
                                onClick = {
                                    brand = b
                                    brandExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Sector / Coordinates") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Interval Slider Selector
                Column {
                    val slideText = if (intervalHours == 0.5f) "30 Mins" else "${intervalHours.toInt()} Hrs"
                    Text(
                        text = "Speech check-in interval: $slideText",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = intervalHours,
                        onValueChange = {
                            intervalHours = when {
                                it <= 0.7f -> 0.5f
                                it <= 1.5f -> 1.0f
                                it <= 2.5f -> 2.0f
                                else -> 3.0f
                            }
                        },
                        valueRange = 0.5f..3.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberGreen,
                            activeTrackColor = CyberGreen
                        )
                    )
                }

                // Plant Type Options (Toggles)
                Text("Special Monitoring Profiles:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isVehicle,
                        onCheckedChange = { isVehicle = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyberGreen)
                    )
                    Text("Main Gate (Vehicles entry screenshot monitor)", color = Color.LightGray, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isFurnace,
                        onCheckedChange = { isFurnace = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyberGreen)
                    )
                    Text("Mining Furnace (Clock on/off logs sheet)", color = Color.LightGray, fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isParking,
                        onCheckedChange = { isParking = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyberGreen)
                    )
                    Text("No Parking Gate Front Obstruct Checker", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, brand, location, intervalHours, isVehicle, isFurnace, isParking)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
            ) {
                Text("INSTALL SCHEDULE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.LightGray)
            }
        },
        containerColor = CyberDarkSurface
    )
}
