package com.example

import android.app.Application
import com.example.data.CctvDatabase
import com.example.data.CctvRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class CctvApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { CctvDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { CctvRepository(database.dao) }
}
