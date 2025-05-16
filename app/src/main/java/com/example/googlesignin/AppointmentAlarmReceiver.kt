package com.example.googlesignin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat


class AppointmentAlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "appointment_channel"
        private var ringtone: Ringtone? = null

        fun stopRingtone() {
            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
            }
            ringtone = null
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Rappels de rendez-vous",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications pour les rappels de rendez-vous"
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentTitle = intent.getStringExtra("appointment_title") ?: "Rendez-vous"
        val appointmentTime = intent.getStringExtra("appointment_time") ?: ""
        val appointmentId = intent.getStringExtra("appointment_id") ?: return

        // Démarrer la sonnerie
        try {
            stopRingtone() // Arrêter toute sonnerie précédente
            ringtone = RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            ringtone?.play()
        } catch (e: Exception) {
            Log.e(
                "AppointmentAlarmReceiver",
                "Erreur lors de la lecture de la sonnerie: ${e.message}"
            )
        }
        showNotification(context, appointmentTitle, appointmentTime, appointmentId)
    }

    private fun showNotification(context: Context, appointmentTitle: String, appointmentTime: String, appointmentId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, HomePageRendezvous::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Rappel de rendez-vous")
            .setContentText("$appointmentTitle à $appointmentTime (dans 2 heures)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(appointmentId.hashCode(), notification)
    }

}