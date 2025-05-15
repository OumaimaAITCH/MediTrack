package com.example.googlesignin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageMedication : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_medication)

        loadMedications()
        setupFab()


    }


    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddMedications::class.java))
        }
    }

//    private fun setupMedicationsList() {
//        val medAdapter = MedAdapter(mutableListOf()) { /* onDelete implementation */ }
//        val rvMedicaments = findViewById<RecyclerView>(R.id.rvMedicaments)
//        rvMedicaments.layoutManager = LinearLayoutManager(this)
//        rvMedicaments.adapter = medAdapter
//
//        // Récupérer l'ID de l'utilisateur connecté
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
//
//        // Modifier la requête pour filtrer par userId
//        db.collection("medications")
//            .whereEqualTo("userId", currentUserId)  // Ajouter le filtre
//            .addSnapshotListener { snapshot, e ->
//                if (e != null) {
//                    Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null) {
//                    val medicamentsList = snapshot.documents.mapNotNull { doc ->
//                        doc.data?.let { data ->
//                            Medicament(
//                                id = doc.id,
//                                nom = data["name"] as? String ?: "",
//                                type = data["type"] as? String ?: "",
//                                dose = data["dose"] as? String ?: "",
//                                quantity = data["quantity"] as? String ?: "",
//                                rappel = data["reminder"] as? String ?: "",
//                                alarmeActive = data["alarmEnabled"] as? Boolean ?: false,
//                                isTaken = data["isTaken"] as? Boolean ?: false
//                            )
//                        }
//                    }
//                    medAdapter.updateMedications(medicamentsList)
//                }
//            }
//    }

    private fun loadMedications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show()
            return
        }

        val medAdapter = MedAdapter(mutableListOf()) { medicament ->
            // Gestion de la suppression si nécessaire
            db.collection("medications")
                .document(medicament.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Médicament supprimé", Toast.LENGTH_SHORT).show()
                    loadMedications() // Recharger la liste
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur lors de la suppression: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        val rvMedicaments = findViewById<RecyclerView>(R.id.rvMedicaments)
        rvMedicaments.layoutManager = LinearLayoutManager(this)
        rvMedicaments.adapter = medAdapter

        db.collection("medications")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val medicamentsList = documents.mapNotNull { doc ->
                    doc.data.let { data ->
                        Medicament(
                            id = doc.id,
                            nom = data["name"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            dose = data["dose"] as? String ?: "",
                            quantity = data["quantity"] as? String ?: "",
                            rappel = data["reminder"] as? String ?: "",
                            alarmeActive = data["alarmEnabled"] as? Boolean ?: false,
                            isTaken = data["isTaken"] as? Boolean ?: false
                        )
                    }
                }
                medAdapter.updateMedications(medicamentsList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadMedications()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


}
