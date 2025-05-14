package com.example.googlesignin

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddMedications : AppCompatActivity() {
    private lateinit var meditrackToolbar: Toolbar
    private lateinit var typeSpinner: Spinner
    private lateinit var reminderInput: EditText
    private lateinit var backIcon: ImageView
    private lateinit var saveButton: Button
    private lateinit var nameInput: EditText
    private lateinit var doseInput: EditText
    private lateinit var quantityInput: EditText
    private lateinit var alarmSwitch: Switch
    private var alarmScheduled = false
    private var alarmTimeInMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medications)

        initializeViews()
        setupToolbar()
        setupSpinner()
        setupReminderInput()
        setupBackIcon()
        setupAlarmSwitch()
        setupSaveButton()
    }

    private fun initializeViews() {
        meditrackToolbar = findViewById(R.id.meditrack_toolbar)
        typeSpinner = findViewById(R.id.type_spinner)
        reminderInput = findViewById(R.id.reminder_input)
        backIcon = findViewById(R.id.back_icon)
        saveButton = findViewById(R.id.save_button)
        nameInput = findViewById(R.id.name_input)
        doseInput = findViewById(R.id.dose_input)
        quantityInput = findViewById(R.id.quantity_input)
        alarmSwitch = findViewById(R.id.alarm_switch)
    }

    private fun setupToolbar() {
        meditrackToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupSpinner() {
        val types = arrayOf("Sélectionnez une option", "Capsule", "Goutte", "Comprimé")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter
    }

    private fun setupReminderInput() {
        reminderInput.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun setupAlarmSwitch() {
        // Désactiver le switch si aucun rappel n'est défini
        reminderInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateAlarmSwitchState()
            }
        }

        // Gérer les changements d'état du switch
        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val reminder = reminderInput.text.toString()
                if (reminder.isEmpty()) {
                    Toast.makeText(this, "Veuillez d'abord définir un rappel", Toast.LENGTH_SHORT).show()
                    alarmSwitch.isChecked = false
                    return@setOnCheckedChangeListener
                }
                // Afficher un message confirmant l'activation de l'alarme
                Toast.makeText(this, "Alarme activée", Toast.LENGTH_SHORT).show()
            } else {
                // Afficher un message confirmant la désactivation de l'alarme
                Toast.makeText(this, "Alarme désactivée", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAlarmSwitchState() {
        val reminder = reminderInput.text.toString()
        if (reminder.isEmpty()) {
            alarmSwitch.isChecked = false
            alarmSwitch.isEnabled = false
        } else {
            alarmSwitch.isEnabled = true
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                }

                // Vérifier si la date est dans le futur
                if (selectedCalendar.timeInMillis <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Veuillez sélectionner une date future", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                alarmTimeInMillis = selectedCalendar.timeInMillis

                val dateTime = String.format(
                    "%02d/%02d/%d, %02d:%02d",
                    selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute
                )
                reminderInput.setText(dateTime)
                updateAlarmSwitchState()
            }, hour, minute, true)
            timePicker.show()
        }, year, month, day)
        datePicker.show()
    }

    private fun setupBackIcon() {
        backIcon.setOnClickListener {
            finish() // Ferme l'activité et retourne à l'écran précédent
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val type = typeSpinner.selectedItem.toString()
            val dose = doseInput.text.toString()
            val quantity = quantityInput.text.toString()
            val reminder = reminderInput.text.toString()
            val alarmeActive = alarmSwitch.isChecked

            // Validation des champs obligatoires
            if (name.isEmpty() || type == "Sélectionnez une option" || dose.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Vérifier si l'alarme est activée mais le rappel n'est pas défini
            if (alarmeActive && reminder.isEmpty()) {
                Toast.makeText(this, "Veuillez définir un rappel pour activer l'alarme", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Création de l'objet Medicament
            val medicament = Medicament(
                nom = name,
                type = type,
                dose = dose,
                quantity = quantity,
                rappel = reminder,
                alarmeActive = alarmeActive
            )

            // Sauvegarde dans Firestore
            val medicationData = hashMapOf(
                "name" to medicament.nom,
                "type" to medicament.type,
                "dose" to medicament.dose,
                "quantity" to medicament.quantity,
                "reminder" to medicament.rappel,
                "alarmEnabled" to medicament.alarmeActive,
                "isTaken" to medicament.isTaken
            )

            FirebaseFirestore.getInstance()
                .collection("medications")
                .add(medicationData)
                .addOnSuccessListener { docRef ->
                    // Si l'alarme est activée, programmer l'alarme
                    if (alarmeActive && reminder.isNotEmpty()) {
                        scheduleAlarm(reminder, name, docRef.id)
                    }
                    Toast.makeText(this, "Médicament ajouté avec succès", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun scheduleAlarm(reminder: String, medicationName: String, medicationId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("medication_name", medicationName)
            putExtra("medication_id", medicationId)
        }

        // Utiliser l'ID du médicament comme code de requête pour éviter les écrasements
        val pendingIntentId = medicationId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (alarmTimeInMillis > 0) {
                // Utiliser le timestamp stocké lors de la sélection de date/heure
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeInMillis, pendingIntent)
                alarmScheduled = true
                Toast.makeText(this, "Alarme programmée pour $reminder", Toast.LENGTH_SHORT).show()
            } else {
                // Méthode alternative si alarmTimeInMillis n'est pas défini
                val dateTimeParts = reminder.split(", ")
                val dateParts = dateTimeParts[0].split("/")
                val timeParts = dateTimeParts[1].split(":")

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.YEAR, dateParts[2].toInt())
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                    set(Calendar.SECOND, 0)
                }

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                alarmScheduled = true
                Toast.makeText(this, "Alarme programmée pour $reminder", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            alarmScheduled = false
            Toast.makeText(this, "Erreur lors de la programmation de l'alarme : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Méthode pour annuler une alarme existante
    private fun cancelAlarm(medicationId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntentId = medicationId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            pendingIntentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Annuler l'alarme
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        alarmScheduled = false
        Toast.makeText(this, "Alarme annulée", Toast.LENGTH_SHORT).show()
    }
}