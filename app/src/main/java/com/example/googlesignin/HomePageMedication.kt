package com.example.googlesignin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class HomePageMedication : AppCompatActivity() {
    private lateinit var adapter: MedAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_medication)

        setupRecyclerView()
        setupFab()
        loadMedications()
    }

    private fun setupRecyclerView() {
        adapter = MedAdapter(mutableListOf()) { medication ->
            // Suppression d'un médicament
            db.collection("medications")
                .document(medication.id)
                .delete()
                .addOnSuccessListener { loadMedications() }
        }

        findViewById<RecyclerView>(R.id.rvMedicaments).apply {
            layoutManager = LinearLayoutManager(this@HomePageMedication)
            adapter = this@HomePageMedication.adapter
        }

    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddMedications::class.java))
        }
    }

    private fun loadMedications() {
        db.collection("medications")
            .get()
            .addOnSuccessListener { documents ->
                val medications = documents.map { doc ->
                    Medicament(
                        id = doc.id,
                        nom = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        dose = doc.getString("dose") ?: "",
                        quantity = doc.getString("quantity") ?: "",
                        rappel = doc.getString("reminder") ?: "",
                        alarmeActive = doc.getBoolean("alarmeActive") ?: false
                    )
                }
                adapter.updateMedications(medications)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Echec de téléchargement", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadMedications()
    }


}
