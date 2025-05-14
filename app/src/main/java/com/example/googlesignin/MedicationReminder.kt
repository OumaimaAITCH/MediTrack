package com.example.googlesignin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class MedicationReminder : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var medication: Medicament? = null

    // Déclaration des vues
    private lateinit var scheduleTextView: TextView
    private lateinit var dosageTextView: TextView
    private lateinit var medicationName: TextView
    private lateinit var backIconImageView: ImageView
    private lateinit var takeButton: Button
    private lateinit var infoIconImageView: ImageView
    private lateinit var deleteIconImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_reminder)

        // Initialisation des vues
        initializeViews()

        // Récupération de l'ID du médicament
        val medId = intent.getStringExtra("medicationId")
        if (medId.isNullOrEmpty()) {
            Toast.makeText(this, "ID de médicament manquant", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadMedication(medId)
        }

        setupBackIcon()
        setupButtons()
    }

    private fun initializeViews() {
        scheduleTextView = findViewById(R.id.tv_schedule)
        dosageTextView = findViewById(R.id.dosage)
        medicationName = findViewById(R.id.medication_name)
        backIconImageView = findViewById(R.id.back_icon)
        takeButton = findViewById(R.id.take_button)
        infoIconImageView = findViewById(R.id.info_icon)
        deleteIconImageView = findViewById(R.id.ivdelete)

    }

    private fun loadMedication(medId: String) {
        db.collection("medications").document(medId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    if (data != null) {
                        medication = Medicament(
                            id = document.id,
                            nom = data["name"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            dose = data["dose"] as? String ?: "",
                            quantity = data["quantity"] as? String ?: "",
                            rappel = data["reminder"] as? String ?: "",
                            alarmeActive = data["alarmEnabled"] as? Boolean ?: false,
                            isTaken = data["isTaken"] as? Boolean ?: false
                        )
                        updateUI()
                    }
                } else {
                    Toast.makeText(this, "Médicament non trouvé", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erreur de chargement: ${exception.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        medication?.let { med ->
            medicationName.text = med.nom

            // Formatage du rappel dans le style souhaité
            val rappelParts = med.rappel.split(", ") // Sépare "21/04/2024, 21:41"
            if (rappelParts.size == 2) {
                val dateParts = rappelParts[0].split("/") // Sépare "21/04/2024"
                val time = rappelParts[1] // "21:41"

                // Convertir en Calendar pour obtenir le jour de la semaine
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.YEAR, dateParts[2].toInt())
                }

                // Obtenir le jour en français
                val joursSemaine = arrayOf("dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
                val jour = joursSemaine[calendar.get(Calendar.DAY_OF_WEEK) - 1]

                // Formater l'affichage
                scheduleTextView.text = "Prévu pour ${time}, ${jour}"
            } else {
                scheduleTextView.text = med.rappel
            }

            dosageTextView.text = "${med.quantity} ${med.type} • ${med.dose}mg"
            infoIconImageView.setImageResource(
                if (med.isTaken) R.drawable.check else R.drawable.attention
            )
        } ?: run {
            Toast.makeText(this, "Impossible de charger les détails du médicament", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupBackIcon() {
        backIconImageView.setOnClickListener { finish() }
    }

    private fun setupButtons() {
        takeButton.setOnClickListener { takeMedication() }
        infoIconImageView.setOnClickListener { showMedicationInfo() }
        deleteIconImageView.setOnClickListener { showDeleteConfirmationDialog() }
    }
    private fun takeMedication() {
        medication?.let { med ->
            med.isTaken = true
            db.collection("medications").document(med.id)
                .set(med)
                .addOnSuccessListener {
                    Toast.makeText(this, "Médicament marqué comme pris", Toast.LENGTH_SHORT).show()
//                    MedicationInfoDialog.newInstance(med).show(supportFragmentManager, "confirmation_dialog")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun editMedication() {
        medication?.let { med ->
            Intent(this, AddMedications::class.java).apply {
                putExtra("medicationId", med.id)
                startActivity(this)
            }
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun showMedicationInfo() {
        medication?.let { med ->
//            MedicationInfoDialog.newInstance(med).show(supportFragmentManager, "info_dialog")
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog() {
        medication?.let { med ->
            AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Voulez-vous vraiment supprimer ${med.nom} ?")
                .setPositiveButton("Oui") { _, _ -> deleteMedication() }
                .setNegativeButton("Non", null)
                .show()
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun deleteMedication() {
        medication?.let { med ->
            db.collection("medications").document(med.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Médicament supprimé", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur de suppression: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        intent.getStringExtra("medicationId")?.let { loadMedication(it) }
    }

}