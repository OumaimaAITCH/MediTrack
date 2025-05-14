package com.example.googlesignin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomePageRendezvous : AppCompatActivity() {
    private lateinit var rvRendezvous: RecyclerView
    private lateinit var ibtnRetour: ImageButton
    private lateinit var adapter: RendezvousAdapter
    private val db = Firebase.firestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page_rendezvous)

        ibtnRetour = findViewById(R.id.ibtnRetour)
        setupRecyclerView()
        loadRendezvous()

        findViewById<FloatingActionButton>(R.id.fabAddRdv).setOnClickListener {
            startActivity(Intent(this, BookAppointements::class.java))
        }
        ibtnRetour.setOnClickListener {
            startActivity(Intent(this, BookAppointements::class.java))
        }
    }
    private fun setupRecyclerView() {
        rvRendezvous = findViewById(R.id.rvRendezvous)
        adapter = RendezvousAdapter(emptyList()) { rendezvous ->
            deleteRendezvous(rendezvous.id)
        }
        rvRendezvous.apply {
            layoutManager = LinearLayoutManager(this@HomePageRendezvous)
            adapter = this@HomePageRendezvous.adapter
        }
    }

    private fun loadRendezvous() {
        db.collection("rendezvous")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                val rendezvousList = documents.map { doc ->
                    Rendezvous(
                        id = doc.id,
                        objet = doc.getString("objet") ?: "",
                        dateHeure = doc.getString("dateHeure") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }
                adapter.updateData(rendezvousList)
            }
    }

    private fun deleteRendezvous(id: String) {
        db.collection("rendezvous")
            .document(id)
            .delete()
            .addOnSuccessListener {
                loadRendezvous()
                Toast.makeText(this, "Rendez-vous supprimé", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadRendezvous()
    }
}