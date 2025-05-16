package com.example.googlesignin.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    val id: String = "",
    val email: String = "",
    val nom: String = "",
    val photoURL: String = "",
    val phoneNumber: String? = null,
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val isActive: Boolean = true,
    val accountType: String = "user",
    val preferences: Map<String, Any>? = null,
    val userMetadata: Map<String, Any>? = null,
    val customFields: Map<String, Any>? = null
)