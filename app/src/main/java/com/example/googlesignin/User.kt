// Classe modèle pour les utilisateurs
package com.example.googlesignin.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId
    val id: String = "",
    val email: String = "",
    val nom: String = "",
    val photoURL: String = "",
    val phoneNumber: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val lastLogin: Timestamp? = null,
    // Ajouter ici d'autres champs personnalisés selon vos besoins
    val isActive: Boolean = true,
    val accountType: String = "user", // Peut être "user", "admin", etc.
    val preferences: Map<String, Any>? = null,
    val userMetadata: Map<String, Any>? = null,
    val customFields: Map<String, Any>? = null
)