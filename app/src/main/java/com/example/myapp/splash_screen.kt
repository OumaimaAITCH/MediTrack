package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.view.View
import android.widget.*
import java.util.*
import kotlin.jvm.java
import kotlin.math.sign


class splash_screen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val btnConnect = findViewById<Button>(R.id.btn_connect)
        btnConnect.setOnClickListener {
            val intent = Intent(this, sign_in::class.java)
            startActivity(intent)

        }
    }
}

