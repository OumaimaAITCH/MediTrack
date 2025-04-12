//package com.example.myapp
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInClient
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.google.android.gms.common.api.ApiException
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.GoogleAuthProvider
//
//class sign_in : AppCompatActivity() {
//    private lateinit var mGoogleSignInClient: GoogleSignInClient
//    private lateinit var firebaseAuth: FirebaseAuth
//    private val RC_SIGN_IN = 123
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_sign_in)
//
//        // Configure Google Sign In
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
//        firebaseAuth = FirebaseAuth.getInstance()
//
//        // Configurer le bouton de connexion Google
//        val signInButton = findViewById<com.google.android.gms.common.SignInButton>(R.id.btn_google_signin)
//        signInButton.setOnClickListener {
//            signIn()
//        }
//
//        // Gestion du bouton retour
//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                finish()
//            }
//        })
//    }
//
//    private fun signIn() {
//        val signInIntent = mGoogleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                firebaseAuthWithGoogle(account.idToken!!)
//            } catch (e: ApiException) {
//                Toast.makeText(this, "Échec de la connexion: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun firebaseAuthWithGoogle(idToken: String) {
//        val credential = GoogleAuthProvider.getCredential(idToken, null)
//        firebaseAuth.signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Connexion réussie
//                    val intent = Intent(this, home::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // Échec de la connexion
//                    Toast.makeText(this, "Authentification échouée", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
//}