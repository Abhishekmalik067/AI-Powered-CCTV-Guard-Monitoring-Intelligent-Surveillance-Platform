package com.example.data

import kotlinx.coroutines.flow.Flow

class CctvRepository(private val dao: CctvDao) {
    val allCameras: Flow<List<CctvCamera>> = dao.getAllCameras()
    val allLogs: Flow<List<SecurityLog>> = dao.getAllLogs()

    suspend fun getCamerasCount(): Int {
        return dao.getCamerasCount()
    }

    suspend fun insertCamera(camera: CctvCamera) {
        dao.insertCamera(camera)
    }

    suspend fun updateCamera(camera: CctvCamera) {
        dao.updateCamera(camera)
    }

    suspend fun insertLog(log: SecurityLog) {
        dao.insertLog(log)
    }

    suspend fun clearAllLogs() {
        dao.clearAllLogs()
    }

    suspend fun checkAndPrepopulate() {
        if (dao.getCamerasCount() == 0) {
            dao.insertCamera(
                CctvCamera(
                    name = "Gate 1 Main Entry",
                    brand = "Dahua",
                    location = "Warehouse Gate Entrance",
                    intervalHours = 0.5f,
                    isRecording = true,
                    status = "Idle",
                    isVehicleMonitor = true
                )
            )
            dao.insertCamera(
                CctvCamera(
                    name = "South Perimeter Wall Checkpoint",
                    brand = "CP Plus",
                    location = "Backyard Yard Area",
                    intervalHours = 1.0f,
                    isRecording = true,
                    status = "Idle",
                    parkingMonitor = true
                )
            )
            dao.insertCamera(
                CctvCamera(
                    name = "Furnace Casting Unit 2",
                    brand = "Hikvision",
                    location = "Induction Furnace Plant B",
                    intervalHours = 2.0f,
                    isRecording = true,
                    status = "Idle",
                    isFurnaceMonitor = true
                )
            )
            dao.insertCamera(
                CctvCamera(
                    name = "Office Frontage Road",
                    brand = "XMEYE",
                    location = "Main Approach Street",
                    intervalHours = 3.0f,
                    isRecording = true,
                    status = "Idle",
                    parkingMonitor = true
                )
            )
            
            dao.insertLog(
                SecurityLog(
                    timestamp = System.currentTimeMillis() - 3600000,
                    cameraName = "System",
                    type = "Status",
                    guardStatus = "N/A",
                    details = "CCTV Operator Automator database initiated.",
                    reportText = "*AUTOMATED REPORT*\nTime: 15:30\nStatus: Online\nSystem setup complete."
                )
            )
        }
    }
}
