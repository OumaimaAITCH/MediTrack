package com.example.googlesignin

data class Medicament(
    var id: String      = "",
    val nom: String     = "",
    val type: String    = "",
    val dose: String    = "",
    val quantite: Int   = 0,
    val alarmeActive: Boolean = false,
    val quantity: String = "",
    val rappel: String = "",
    var isTaken: Boolean = false
)

