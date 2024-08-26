package com.example.mobile_apps_2024

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.mobile_apps_2024.R
import com.example.mobile_apps_2024.SupabaseClient

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val errorMessage = findViewById<TextView>(R.id.error_message)

        loginButton.setOnClickListener{
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            SupabaseClient.client.auth.signIn(email, password)
                .thenAccept{ session ->
                    if(session != null) {
                    startActivity(Intent(this, Display::class.java))
                    finish()
                } else {
                    errorMessage.text = "Login Failed."
                }
        }
                .exceptionally {
                    errorMessage.text = it.message
                    null
                }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}