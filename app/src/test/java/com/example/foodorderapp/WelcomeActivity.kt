package com.example.foodorderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnBuyer = findViewById<Button>(R.id.btnBuyer)
        val btnSeller = findViewById<Button>(R.id.btnSeller)
        val tvGoToSignIn = findViewById<TextView>(R.id.tvGoToSignIn)

        btnBuyer.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("role", "buyer")
            startActivity(intent)
        }

        btnSeller.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("role", "seller")
            startActivity(intent)
        }

        tvGoToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}