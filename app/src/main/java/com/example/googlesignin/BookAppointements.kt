package com.example.googlesignin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.toString

class BookAppointements : AppCompatActivity() {
    private lateinit var edDateTime: EditText
    private lateinit var ibtnRetour: ImageButton
    private lateinit var btnConfirmer: Button
    private lateinit var btnAnnuler: Button
    private lateinit var ivAllRendezvous: ImageView
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
    private lateinit var tvNomRendezvous: EditText
    private val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointements)

        initializeViews()
        setupClickListeners()


        ibtnRetour.setOnClickListener {
            startActivity(Intent(this, Home::class.java))
        }
    }
    private fun initializeViews() {
        edDateTime = findViewById(R.id.edatetime)
        ibtnRetour = findViewById(R.id.ibtnRetour)
        btnConfirmer = findViewById(R.id.btnConfirmer)
        btnAnnuler = findViewById(R.id.btnAnnuler)
        tvNomRendezvous = findViewById(R.id.tv_nomRendezvous)
        ivAllRendezvous = findViewById(R.id.ivAllRendezvous)

    }

    private fun setupClickListeners() {
        edDateTime.setOnClickListener {
            showDateTimePicker()
        }

        btnConfirmer.setOnClickListener {
            showConfirmationDialog(true)
        }

        btnAnnuler.setOnClickListener {
            showConfirmationDialog(false)
        }
        ivAllRendezvous.setOnClickListener {
            startActivity(Intent(this, HomePageRendezvous::class.java))
        }
    }

    private fun saveRendezVous() {
        val objet = tvNomRendezvous.text.toString()
        val dateHeure = edDateTime.text.toString()

        if (objet.isEmpty() || dateHeure.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val rendezvous = hashMapOf(
            "objet" to objet,
            "dateHeure" to dateHeure,
            "timestamp" to calendar.timeInMillis
        )

        db.collection("rendezvous")
            .add(rendezvous)
            .addOnSuccessListener {
                Toast.makeText(this, "Rendez-vous enregistré", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomePageRendezvous::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDateTimePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        edDateTime.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showConfirmationDialog(isConfirm: Boolean) {
        val title = if (isConfirm) "Confirmation du Rendez-vous" else "Annulation du Rendez-vous"
        val message = if (isConfirm)
            "Voulez-vous confirmer ce rendez-vous ?"
        else
            "Voulez-vous annuler ce rendez-vous ?"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Oui") { _, _ ->
                if (isConfirm) {
                    // Code to confirm the appointment sauvegarder dans database
                    saveRendezVous()
                }
                finish()
            }
            .setNegativeButton("Non", null)
            .setCancelable(false)
            .show()
    }
}