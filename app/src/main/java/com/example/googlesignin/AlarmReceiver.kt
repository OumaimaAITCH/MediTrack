package com.example.googlesignin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "Médicament"
        val medicationId = intent.getStringExtra("medication_id")

        Log.d(TAG, "Alarme reçue pour $medicationName")

        // Créer la notification pour rappeler de prendre le médicament
        showNotification(context, medicationName, medicationId)
    }
    private fun showNotification(context: Context, medicationName: String, medicationId: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_channel"

        // Créer un intent pour ouvrir l'application quand la notification est cliquée
        val contentIntent = Intent(context, Home::class.java)
        contentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Créer un intent pour marquer le médicament comme pris
        val takeMedicationIntent = Intent(context, TakeMedicationReceiver::class.java).apply {
            action = "ACTION_TAKE_MEDICATION"
            putExtra("medication_id", medicationId)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            takeMedicationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configurer le son de notification
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Créer le canal de notification pour Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Rappels de médicaments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les rappels de médicaments"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Construire la notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Rappel de médicament")
            .setContentText("Il est temps de prendre votre $medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.check, "Pris", takePendingIntent)

        // Afficher la notification
        val notificationId = medicationId?.hashCode() ?: System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


}