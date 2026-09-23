package com.streamx.app

import android.app.Application
import com.streamx.app.data.AppDatabase
import com.google.firebase.FirebaseApp

class StreamXApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        database = AppDatabase.getDatabase(this)
    }
}
