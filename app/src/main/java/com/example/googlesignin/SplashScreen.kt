package com.example.googlesignin

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.Intent
import android.widget.*
import kotlin.jvm.java


class SplashScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val btnConnect = findViewById<Button>(R.id.btn_connect)
        btnConnect.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)

        }
    }
}

