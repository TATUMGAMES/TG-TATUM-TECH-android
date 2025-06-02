package com.tatumgames.tatumtech.android.application

import android.app.Application
import com.google.firebase.FirebaseApp

class TatumTechApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
