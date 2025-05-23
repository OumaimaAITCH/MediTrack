package com.example.googlesignin

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialisation de Firebase
        FirebaseApp.initializeApp(this)

        // Création des canaux de notification
        AlarmReceiver.createNotificationChannel(this)
        AppointmentAlarmReceiver.createNotificationChannel(this)
    }

}