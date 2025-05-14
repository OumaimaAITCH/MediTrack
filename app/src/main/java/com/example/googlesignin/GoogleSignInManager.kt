package com.example.googlesignin

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class GoogleSignInManager private constructor() {
    private var context: Context? = null
    private var activity: Activity? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null

    val GOOGLE_SIGN_IN = 100
    private var firebaseAuth: FirebaseAuth? = null


    private fun init(context: Context) {
        this.context = context
        activity = context as Activity
    }

    fun setupGoogleSignInOptions() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context!!.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    companion object {
        private var instance: GoogleSignInManager? = null

        fun getInstance(context: Context): GoogleSignInManager? {
            if (instance == null) {
                instance = GoogleSignInManager()
            }
            instance!!.init(context)
            return instance
        }
    }

    val isUserSignedIn: Boolean
        get() {
            val currentUser = firebaseAuth!!.currentUser
            return currentUser != null
        }

    fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        activity!!.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient!!.signOut()
        Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
    }

    val profileInfo: FirebaseUser?
        get() = FirebaseAuth.getInstance().currentUser

    fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("TAG", "Échec de connexion: ${e.statusCode}")
            Toast.makeText(context, "Échec de la connexion", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    Log.d("TAG", "Connexion réussie")
                    val intent = Intent(context, Home::class.java)
                    context?.startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(context, "Une erreur s'est produite", Toast.LENGTH_SHORT).show()
                    Log.w("TAG", "Échec de l'authentification", task.exception)
                }
            }
    }
}