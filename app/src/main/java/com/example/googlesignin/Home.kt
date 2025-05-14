package com.example.googlesignin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class Home : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnMed: Button
    private lateinit var btnRdv: Button
    private lateinit var txtLogout: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        btnMed = findViewById(R.id.btn_med)
        btnRdv = findViewById(R.id.btn_rdv)
        txtLogout = findViewById(R.id.txt_logout)

        auth = FirebaseAuth.getInstance()

        // Configuration de Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnMed.setOnClickListener {
            startActivity(Intent(this, HomePageMedication::class.java))
        }

        // Gestionnaire de clic pour le bouton Rendez-vous
        btnRdv.setOnClickListener {
            startActivity(Intent(this, BookAppointements::class.java))
        }

        // Gestionnaire de clic pour la déconnexion
        findViewById<TextView>(R.id.txt_logout).setOnClickListener {
            signOut()
        }


    }

    private fun signOut() {
        // Déconnexion de Firebase
        auth.signOut()

        // Déconnexion de Google SignIn
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // Redirection vers la page de connexion
            val intent = Intent(this, SignIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}