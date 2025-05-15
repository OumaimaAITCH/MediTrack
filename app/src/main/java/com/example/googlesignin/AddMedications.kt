package com.example.googlesignin


import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth

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
    private lateinit var dureeInput: EditText

    // Variables for edit mode
    private var isEditMode = false
    private var medicationId = ""
    private var originalAlarmEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medications)
        dureeInput = findViewById(R.id.duree_input)

        initializeViews()
        setupToolbar()
        setupSpinner()
        setupReminderInput()
        setupBackIcon()
        setupAlarmSwitch()
        setupSaveButton()

        // Check if we're in edit mode
        checkForEditMode()
    }

    private fun checkForEditMode() {
        val extras = intent.extras
        if (extras != null && extras.containsKey("MEDICATION_ID")) {
            isEditMode = true
            medicationId = extras.getString("MEDICATION_ID", "")

            // Update toolbar title
            meditrackToolbar.title = "Modifier le médicament"

            // Load medication data
            loadMedicationData(medicationId)

            // Update button text
            saveButton.text = "Mettre à jour"
        }
    }

    private fun loadMedicationData(id: String) {
        if (id.isEmpty()) return

        FirebaseFirestore.getInstance()
            .collection("medications")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Fill form with existing data
                    nameInput.setText(document.getString("name") ?: "")
                    doseInput.setText(document.getString("dose") ?: "")
                    quantityInput.setText(document.getString("quantity") ?: "")
                    reminderInput.setText(document.getString("reminder") ?: "")
                    dureeInput.setText((document.getLong("duree") ?: 0).toString())

                    // Set alarm switch
                    val alarmEnabled = document.getBoolean("alarmEnabled") ?: false
                    originalAlarmEnabled = alarmEnabled
                    alarmSwitch.isChecked = alarmEnabled
                    alarmSwitch.isEnabled = !reminderInput.text.toString().isEmpty()

                    // Set spinner selection
                    val medicationType = document.getString("type") ?: ""
                    setSpinnerSelection(medicationType)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur lors du chargement des données: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setSpinnerSelection(type: String) {
        val adapter = typeSpinner.adapter as SimpleAdapter
        for (i in 0 until adapter.count) {
            val item = adapter.getItem(i) as Map<*, *>
            if (item["text"] == type) {
                typeSpinner.setSelection(i)
                break
            }
        }
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
        val itemList = listOf(
            mapOf("text" to "Sélectionnez une option", "image" to null),
            mapOf("text" to "Capsule", "image" to R.drawable.capsule),
            mapOf("text" to "Goutte", "image" to R.drawable.goutte),
            mapOf("text" to "Comprimé", "image" to R.drawable.comprimes)
        )

        val adapter = SimpleAdapter(
            this,
            itemList,
            R.layout.spinner_item,
            arrayOf("text", "image"),
            intArrayOf(R.id.text, R.id.icon)
        )

        adapter.setDropDownViewResource(R.layout.spinner_item)
        typeSpinner.adapter = adapter
    }

    private fun setupReminderInput() {
        reminderInput.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun setupAlarmSwitch() {
        reminderInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateAlarmSwitchState()
            }
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val reminder = reminderInput.text.toString()
                if (reminder.isEmpty()) {
                    Toast.makeText(this, "Veuillez d'abord définir un rappel", Toast.LENGTH_SHORT).show()
                    alarmSwitch.isChecked = false
                    return@setOnCheckedChangeListener
                }
                Toast.makeText(this, "Alarme activée", Toast.LENGTH_SHORT).show()
            } else {
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
            finish()
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val spinnerItem = typeSpinner.selectedItem as? Map<*, *>
            val type = spinnerItem?.get("text")?.toString() ?: "Sélectionnez une option"
            val dose = doseInput.text.toString()
            val quantity = quantityInput.text.toString()
            val reminder = reminderInput.text.toString()
            val alarmeActive = alarmSwitch.isChecked
            val duree = dureeInput.text.toString().toIntOrNull() ?: 0
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId == null) {
                Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medicationData = hashMapOf(
                "name" to name,
                "type" to type,
                "dose" to dose,
                "quantity" to quantity,
                "reminder" to reminder,
                "alarmEnabled" to alarmeActive,
                "isTaken" to false,
                "duree" to duree,
                "userId" to currentUserId  // Ajout de l'ID utilisateur
            )


            if (name.isEmpty() || type == "Sélectionnez une option" || dose.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs obligatoires (*)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (alarmeActive && reminder.isEmpty()) {
                Toast.makeText(this, "Veuillez définir un rappel pour activer l'alarme", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val db = FirebaseFirestore.getInstance()

            if (isEditMode) {
                // Update existing medication
                db.collection("medications")
                    .document(medicationId)
                    .update(medicationData as Map<String, Any>)
                    .addOnSuccessListener {
                        // Cancel existing alarm if it was enabled and now disabled or reminder changed
                        if (originalAlarmEnabled && (!alarmeActive || originalAlarmEnabled != alarmeActive)) {
                            cancelAlarm(medicationId)
                        }

                        // Schedule new alarm if enabled
                        if (alarmeActive && reminder.isNotEmpty()) {
                            scheduleAlarm(reminder, name, medicationId)
                        }

                        Toast.makeText(this, "Médicament mis à jour avec succès", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erreur lors de la mise à jour : ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Add new medication
                db.collection("medications")
                    .add(medicationData)
                    .addOnSuccessListener { docRef ->
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
    }

    private fun scheduleAlarm(reminder: String, medicationName: String, medicationId: String) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val duree = dureeInput.text.toString().toIntOrNull() ?: 0

            // First cancel any existing alarms for this medication
            cancelAlarm(medicationId)

            val reminderParts = reminder.split(", ")
            if (reminderParts.size == 2) {
                val dateParts = reminderParts[0].split("/")
                val timeParts = reminderParts[1].split(":")

                // Créer les alarmes pour chaque jour de la durée
                for (day in 0 until duree) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
                        set(Calendar.MONTH, dateParts[1].toInt() - 1)
                        set(Calendar.YEAR, dateParts[2].toInt())
                        set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                        set(Calendar.MINUTE, timeParts[1].toInt())
                        set(Calendar.SECOND, 0)
                        add(Calendar.DAY_OF_MONTH, day)  // Ajouter les jours
                    }

                    val intent = Intent(this, AlarmReceiver::class.java).apply {
                        putExtra("medication_name", medicationName)
                        putExtra("medication_id", medicationId)
                        putExtra("day_number", day + 1)
                        putExtra("total_days", duree)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        "${medicationId}_${day}".hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setAlarmClock(
                                AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                                pendingIntent
                            )
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
                Toast.makeText(this, "Alarmes programmées pour $duree jours", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelAlarm(medicationId: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val duree = dureeInput.text.toString().toIntOrNull() ?: 0

        // Cancel all alarms for this medication (for each day)
        for (day in 0 until duree) {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntentId = "${medicationId}_${day}".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                pendingIntentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            } catch (e: Exception) {
                // Just skip if there's an error with a particular alarm
            }
        }

        // Also try to cancel using the original format (for backward compatibility)
        try {
            val intent = Intent(this, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                medicationId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            // Just skip if there's an error
        }

        alarmScheduled = false
    }
}