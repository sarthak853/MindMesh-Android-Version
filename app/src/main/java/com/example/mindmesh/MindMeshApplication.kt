package com.example.mindmesh

import android.app.Application
import com.example.mindmesh.data.database.MindMeshDatabase

class MindMeshApplication : Application() {
    
    val database by lazy { MindMeshDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
    }
}