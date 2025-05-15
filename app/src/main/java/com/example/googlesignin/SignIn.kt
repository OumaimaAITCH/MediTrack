package com.example.googlesignin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

class SignIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialiser Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configurer Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        // Configurer le bouton de connexion
        findViewById<SignInButton>(R.id.signInButton).apply {
            setSize(SignInButton.SIZE_WIDE)
            setOnClickListener {
                val signInIntent = googleClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Échec de la connexion Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Récupérer l'utilisateur Firebase
                    val user = auth.currentUser

                    // Vérifier si l'utilisateur existe
                    if (user != null) {
                        // Créer un objet Map avec les données de l'utilisateur
                        val userData = hashMapOf(
                            "email" to user.email,
                            "nom" to user.displayName,
                            "photoURL" to (user.photoUrl?.toString() ?: ""),
                            "lastLogin" to Date(),
                            "phoneNumber" to (user.phoneNumber ?: "")
                        )

                        // Référence au document utilisateur dans Firestore
                        val userRef = db.collection("users").document(user.uid)

                        // Vérifier si l'utilisateur existe déjà dans Firestore
                        userRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // L'utilisateur existe, mettre à jour la dernière connexion
                                    userRef.set(
                                        hashMapOf("lastLogin" to Date()),
                                        SetOptions.merge()
                                    )
                                    Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Créer un nouveau document utilisateur
                                    userData["createdAt"] = Date()
                                    userRef.set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Nouvel utilisateur enregistré", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Erreur d'enregistrement: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }

                                // Redirection vers l'activité principale
                                val intent = Intent(this, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Erreur Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Erreur d'authentification Firebase.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}