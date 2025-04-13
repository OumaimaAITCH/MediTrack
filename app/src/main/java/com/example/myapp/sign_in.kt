package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.SignInButton
import android.widget.TextView

class sign_in : AppCompatActivity() {
    private var googleSignInManager: GoogleSignInManager? = null
    private lateinit var signInButton: SignInButton
    private lateinit var textInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialisation des vues
        signInButton = findViewById(R.id.signInButton)
        textInfo = findViewById(R.id.textViewSignIn)

        // Configuration du gestionnaire de connexion Google
        googleSignInManager = GoogleSignInManager.getInstance(this)
        googleSignInManager?.setupGoogleSignInOptions()

        // Gestion du clic sur le bouton de connexion
        signInButton.setOnClickListener {
            googleSignInManager?.signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        if (googleSignInManager?.isUserSignedIn == true) {
            Toast.makeText(this, "Déjà connecté", Toast.LENGTH_SHORT).show()
            // Rediriger vers l'activité principale si déjà connecté
            startActivity(Intent(this, home::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == googleSignInManager?.GOOGLE_SIGN_IN) {
            googleSignInManager?.handleSignInResult(data)
        }
    }
}