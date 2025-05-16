// Classe utilitaire pour gérer les utilisateurs dans Firestore
package com.example.googlesignin.utils

import com.example.googlesignin.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class UserManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    /**
     * Crée ou met à jour un utilisateur dans Firestore après une connexion Google
     */
    fun saveUserToFirestore(firebaseUser: FirebaseUser, additionalData: Map<String, Any>? = null): Task<Void> {
        val userRef = usersCollection.document(firebaseUser.uid)

        // Données de base de l'utilisateur avec type explicite
        val userData = mutableMapOf<String, Any>(
            "email" to (firebaseUser.email ?: ""),
            "nom" to (firebaseUser.displayName ?: ""),
            "photoURL" to (firebaseUser.photoUrl?.toString() ?: ""),
            "lastLogin" to Date(),
            "phoneNumber" to (firebaseUser.phoneNumber ?: "")
        )

        // Ajouter des données supplémentaires si elles sont fournies
        additionalData?.forEach { (key, value) ->
            userData[key] = value
        }

        // Mettre à jour le document avec SetOptions.merge()
        return userRef.set(userData, SetOptions.merge())
    }

    fun checkIfUserExists(userId: String): Task<DocumentSnapshot> {
        return usersCollection.document(userId).get()
    }

   
    suspend fun getCurrentUser(): User? = suspendCoroutine { continuation ->
        val currentUser = auth.currentUser
        if (currentUser == null) {
            continuation.resume(null)
            return@suspendCoroutine
        }

        usersCollection.document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = User(
                        id = document.id,
                        email = document.getString("email") ?: "",
                        nom = document.getString("nom") ?: "",
                        photoURL = document.getString("photoURL") ?: "",
                        phoneNumber = document.getString("phoneNumber"),
                        createdAt = document.getTimestamp("createdAt"),
                        lastLogin = document.getTimestamp("lastLogin")
                    )
                    continuation.resume(user)
                } else {
                    continuation.resume(null)
                }
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    /**
     * Met à jour des champs spécifiques de l'utilisateur
     */
    fun updateUserFields(userId: String, fieldsToUpdate: Map<String, Any>): Task<Void> {
        return usersCollection.document(userId).update(fieldsToUpdate)
    }

    /**
     * Ajoute un champ personnalisé à un utilisateur existant
     */
    fun addCustomField(userId: String, fieldName: String, fieldValue: Any): Task<Void> {
        val data = hashMapOf<String, Any>(
            "customFields.$fieldName" to fieldValue
        )
        return usersCollection.document(userId).set(data, SetOptions.merge())
    }

    /**
     * Supprime un utilisateur de Firestore (marque comme inactif)
     * Note: Cela ne supprime pas l'authentification
     */
    fun deactivateUser(userId: String): Task<Void> {
        return usersCollection.document(userId).update("isActive", false)
    }

    /**
     * Obtient l'ID utilisateur actuel
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}