package com.fairshare

import android.app.Application
import com.google.firebase.FirebaseApp

class FairShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 