package com.example.googlesignin

import android.annotation.SuppressLint
import android.app.AlarmManager
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
import com.google.firebase.firestore.FirebaseFirestore


class AlarmReceiver: BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    companion object {
        private var ringtone: Ringtone? = null
        private const val CHANNEL_ID = "medication_channel"

        fun stopRingtone() {
            if (ringtone?.isPlaying == true) {
                ringtone?.stop()
            }
            ringtone = null
        }

        @SuppressLint("ServiceCast")
        fun cancelFutureAlarms(context: Context, medicationId: String, totalDays: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Annuler toutes les alarmes futures pour chaque jour
            for (day in 0 until totalDays) {
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("medication_id", medicationId)
                }

                val pendingIntentId = "${medicationId}_${day}".hashCode()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    pendingIntentId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                } catch (e: Exception) {
                    Log.e("AlarmReceiver", "Erreur lors de l'annulation de l'alarme: ${e.message}")
                }
            }

            // Annuler également l'alarme au format original (pour la compatibilité)
            try {
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medicationId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Erreur lors de l'annulation de l'alarme originale: ${e.message}")
            }

            // Fermer la notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(medicationId.hashCode())
        }

        // Créer le canal de notification une seule fois lors du démarrage de l'application
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Rappels de médicaments",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications pour les rappels de médicaments"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)
                    setShowBadge(true)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }


    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "Médicament"
        val medicationId = intent.getStringExtra("medication_id") ?: return
        val dayNumber = intent.getIntExtra("day_number", 0)
        val totalDays = intent.getIntExtra("total_days", 0)

        Log.d(TAG, "Alarme reçue pour $medicationName (ID: $medicationId), jour $dayNumber/$totalDays")

        // Vérifier si le médicament est déjà marqué comme pris
        val db = FirebaseFirestore.getInstance()
        db.collection("medications").document(medicationId)
            .get()
            .addOnSuccessListener { document ->
                val isTaken = document.getBoolean("isTaken") ?: false

                if (isTaken) {
                    Log.d(TAG, "Le médicament $medicationName est déjà marqué comme pris. Pas de notification.")
                    return@addOnSuccessListener
                }

                // Jouer une sonnerie si le médicament n'est pas pris
                try {
                    stopRingtone() // Arrêter toute sonnerie précédente
                    ringtone = RingtoneManager.getRingtone(
                        context,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    )
                    ringtone?.play()
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la lecture de la sonnerie: ${e.message}")
                }

                // Afficher la notification
                showNotification(context, medicationName, medicationId, dayNumber, totalDays)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la vérification du statut du médicament: ${e.message}")
                // En cas d'erreur, afficher quand même la notification
                showNotification(context, medicationName, medicationId, dayNumber, totalDays)
            }
    }

    private fun showNotification(context: Context, medicationName: String, medicationId: String, dayNumber: Int, totalDays: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal de notification pour Android 8.0+
        createNotificationChannel(context)

        // Créer un intent pour ouvrir MedicationReminder quand la notification est cliquée
        val contentIntent = Intent(context, MedicationReminder::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("medicationId", medicationId)
        }

        // Créer un intent pour marquer le médicament comme pris
        val takeMedicationIntent = Intent(context, TakeMedicationReceiver::class.java).apply {
            action = "ACTION_TAKE_MEDICATION"
            putExtra("medication_id", medicationId)
            putExtra("stop_ringtone", true)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            medicationId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (medicationId + "_take").hashCode(),
            takeMedicationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construire la notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("Rappel de médicament (Jour $dayNumber/$totalDays)")
            .setContentText("Il est temps de prendre votre $medicationName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setAutoCancel(true)
            .setProgress(totalDays, dayNumber, false)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.check, "Pris", takePendingIntent)
            .setOngoing(true) // Rendre la notification persistante


        // Afficher la notification
        val notificationId = medicationId.hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


}