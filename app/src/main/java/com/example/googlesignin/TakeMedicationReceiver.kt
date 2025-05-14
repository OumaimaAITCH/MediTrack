package com.example.googlesignin

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class TakeMedicationReceiver {
    private val TAG = "TakeMedicationReceiver"

    fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_TAKE_MEDICATION") {
            val medicationId = intent.getStringExtra("medication_id")
            if (medicationId != null) {
                markMedicationAsTaken(context, medicationId)
            }
        }
    }

    @SuppressLint("ServiceCast")
    private fun markMedicationAsTaken(context: Context, medicationId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("medications").document(medicationId)
            .update("isTaken", true)
            .addOnSuccessListener {
                Log.d(TAG, "Médicament marqué comme pris")

                // Fermer la notification
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(medicationId.hashCode())
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Erreur lors de la mise à jour du statut du médicament: ${e.message}")
            }
    }
}