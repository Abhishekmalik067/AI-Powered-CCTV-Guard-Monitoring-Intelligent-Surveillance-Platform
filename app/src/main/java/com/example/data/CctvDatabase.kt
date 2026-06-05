package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "cameras")
data class CctvCamera(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val brand: String, // Dahua, CP Plus, Hanwha, Hikvision, XMEYE, EseeCloud
    val location: String,
    val intervalHours: Float, // e.g. 0.5 for 30m, 1.0, 2.0, 3.0
    val isRecording: Boolean = false,
    val status: String = "Idle", // Idle, Active Check, Siren Alert
    val lastCheckTime: Long = 0,
    val isVehicleMonitor: Boolean = false,
    val isFurnaceMonitor: Boolean = false,
    val parkingMonitor: Boolean = false
)

@Entity(tableName = "logs")
data class SecurityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val cameraName: String,
    val type: String, // "Guard 2-Way", "Vehicle Entry", "Furnace State", "Illegal Parking", "Intrusion"
    val guardStatus: String, // "Active", "Sleeping", "No Response", "Using Mobile", "N/A"
    val details: String,
    val reportText: String
)

@Dao
interface CctvDao {
    @Query("SELECT * FROM cameras ORDER BY id ASC")
    fun getAllCameras(): Flow<List<CctvCamera>>

    @Query("SELECT COUNT(*) FROM cameras")
    suspend fun getCamerasCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCamera(camera: CctvCamera)

    @Update
    suspend fun updateCamera(camera: CctvCamera)

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SecurityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityLog)

    @Query("DELETE FROM logs")
    suspend fun clearAllLogs()
}

@Database(entities = [CctvCamera::class, SecurityLog::class], version = 1, exportSchema = false)
abstract class CctvDatabase : RoomDatabase() {
    abstract val dao: CctvDao

    companion object {
        @Volatile
        private var INSTANCE: CctvDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): CctvDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CctvDatabase::class.java,
                    "cctv_automator_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
