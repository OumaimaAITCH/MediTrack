package com.example.googlesignin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore


class TakeMedicationReceiver : BroadcastReceiver() {
    private val TAG = "TakeMedicationReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "TakeMedicationReceiver onReceive")

        // Extraire l'ID du médicament de l'intent
        val medicationId = intent.getStringExtra("medication_id")
        val stopRingtone = intent.getBooleanExtra("stop_ringtone", false)

        if (medicationId == null) {
            Log.e(TAG, "Pas d'ID de médicament fourni")
            return
        }

        // Arrêter la sonnerie si demandé
        if (stopRingtone) {
            AlarmReceiver.stopRingtone()
        }

        // Fermer la notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(medicationId.hashCode())

        // Récupérer les informations du médicament depuis Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("medications")
            .document(medicationId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val totalDays = document.getLong("duree")?.toInt() ?: 0

                    // Annuler toutes les alarmes futures
                    AlarmReceiver.cancelFutureAlarms(context, medicationId, totalDays)

                    // Marquer le médicament comme pris
                    db.collection("medications")
                        .document(medicationId)
                        .update(
                            mapOf(
                                "isTaken" to true,
                                "alarmEnabled" to false
                            )
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "Médicament marqué comme pris avec succès")
                            Toast.makeText(context, "Médicament marqué comme pris", Toast.LENGTH_SHORT).show()

                            // Ouvrir l'écran d'accueil
                            val homeIntent = Intent(context, HomePageMedication::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(homeIntent)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Erreur lors de la mise à jour du statut du médicament: ${e.message}")
                            Toast.makeText(context, "Erreur lors de la mise à jour: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(TAG, "Document du médicament non trouvé")
                    Toast.makeText(context, "Médicament non trouvé", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la récupération du médicament: ${e.message}")
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}