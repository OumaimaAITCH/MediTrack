package com.example.googlesignin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager

class MedicationReminder : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private var medication: Medicament? = null

    private lateinit var scheduleTextView: TextView
    private lateinit var dosageTextView: TextView
    private lateinit var medicationName: TextView
    private lateinit var backIconImageView: ImageView
    private lateinit var takeButton: Button
    private lateinit var infoIconImageView: ImageView
    private lateinit var deleteIconImageView: ImageView
    private lateinit var editIconImageView: ImageView

    // Gestion des permissions
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { (permission, granted) ->
                if (!granted) {
                    Toast.makeText(this, "Permission $permission refusée", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_reminder)

        // Initialiser le canal de notification
        AlarmReceiver.createNotificationChannel(this)

        // Vérifier les permissions
        checkPermissions()

        initializeViews()

        // Récupération de l'ID du médicament
        val medId = intent.getStringExtra("medicationId")
        if (medId.isNullOrEmpty()) {
            Toast.makeText(this, "ID de médicament manquant", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadMedication(medId)
        }

        // Arrêter la sonnerie si elle est active
        AlarmReceiver.stopRingtone()

        setupBackIcon()
        setupButtons()
    }

    @SuppressLint("ServiceCast")
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
        if (permissions.isNotEmpty()) {
            requestPermissions.launch(permissions.toTypedArray())
        }
    }

    private fun initializeViews() {
        scheduleTextView = findViewById(R.id.tv_schedule)
        dosageTextView = findViewById(R.id.dosage)
        medicationName = findViewById(R.id.medication_name)
        backIconImageView = findViewById(R.id.back_icon)
        takeButton = findViewById(R.id.take_button)
        infoIconImageView = findViewById(R.id.info_icon)
        deleteIconImageView = findViewById(R.id.ivdelete)

        try {
            editIconImageView = findViewById(R.id.info_icon) // Corrigé : utiliser edit_icon
            editIconImageView.setOnClickListener { editMedication() }
        } catch (e: Exception) {
            Log.e("MedicationReminder", "Edit icon not found in layout: ${e.message}")
        }
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

    private fun setupBackIcon() {
        backIconImageView.setOnClickListener {
            medication?.let { med ->
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(med.id.hashCode())
            }
            finish()
        }
    }

    private fun setupButtons() {
        takeButton.setOnClickListener { takeMedication() }
        infoIconImageView.setOnClickListener { showMedicationInfo() }
        deleteIconImageView.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun takeMedication() {
        medication?.let { med ->
            AlarmReceiver.stopRingtone()
            db.collection("medications").document(med.id)
                .get()
                .addOnSuccessListener { document ->
                    val totalDays = document.getLong("duree")?.toInt() ?: 0
                    try {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(med.id.hashCode())
                    } catch (e: Exception) {
                        Log.e("MedicationReminder", "Erreur lors de la suppression de la notification: ${e.message}")
                    }
                    AlarmReceiver.cancelFutureAlarms(this, med.id, totalDays)
                    db.collection("medications").document(med.id)
                        .update(
                            mapOf(
                                "isTaken" to true,
                                "alarmEnabled" to false
                            )
                        )
                        .addOnSuccessListener {
                            Toast.makeText(this, "Médicament marqué comme pris", Toast.LENGTH_SHORT).show()
                            medication?.isTaken = true
                            updateUI()
                            val intent = Intent(this, HomePageMedication::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur lors de la récupération des données: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        medication?.let { med ->
            medicationName.text = med.nom.uppercase()
            val rappelParts = med.rappel.split(", ")
            if (rappelParts.size == 2) {
                val dateParts = rappelParts[0].split("/")
                val time = rappelParts[1]
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.YEAR, dateParts[2].toInt())
                }
                val joursSemaine = arrayOf("dimanche", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi")
                val jour = joursSemaine[calendar.get(Calendar.DAY_OF_WEEK) - 1]
                scheduleTextView.text = "Prévu pour ${time}, ${jour}"
            } else {
                scheduleTextView.text = med.rappel
            }
            dosageTextView.text = "${med.quantity} ${med.type} • ${med.dose}mg"
            infoIconImageView.setImageResource(
                if (med.isTaken) R.drawable.check else R.drawable.attention
            )
            if (med.isTaken) {
                takeButton.text = "Déjà pris"
                takeButton.isEnabled = false
                takeButton.background = ContextCompat.getDrawable(this, R.drawable.button_disabled_background)
            } else {
                takeButton.text = "Prendre"
                takeButton.isEnabled = true
                takeButton.background = ContextCompat.getDrawable(this, R.drawable.button_background)
            }
        } ?: run {
            Toast.makeText(this, "Impossible de charger les détails du médicament", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun editMedication() {
        medication?.let { med ->
            Intent(this, AddMedications::class.java).apply {
                putExtra("MEDICATION_ID", med.id)
                startActivity(this)
            }
        } ?: Toast.makeText(this, "Données du médicament non disponibles", Toast.LENGTH_SHORT).show()
    }

    private fun showMedicationInfo() {
        medication?.let { med ->
            val message = """
                Nom: ${med.nom}
                Type: ${med.type}
                Dose: ${med.dose}mg
                Quantité: ${med.quantity}
                Rappel: ${med.rappel}
                Alarme active: ${if (med.alarmeActive) "Oui" else "Non"}
                Statut: ${if (med.isTaken) "Pris" else "Pas encore pris"}
            """.trimIndent()
            AlertDialog.Builder(this)
                .setTitle("Informations sur le médicament")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
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
            val totalDays = 30
            AlarmReceiver.cancelFutureAlarms(this, med.id, totalDays)
            db.collection("medications").document(med.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "${med.nom} supprimé", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomePageMedication::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
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

    override fun onDestroy() {
        super.onDestroy()
        AlarmReceiver.stopRingtone()
    }
}