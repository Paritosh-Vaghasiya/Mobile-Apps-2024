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

class Signup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val firstNameEditText = findViewById<EditText>(R.id.first_name)
        val lastNameEditText = findViewById<EditText>(R.id.last_name)
        val cityEditText = findViewById<EditText>(R.id.city)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val signupButton = findViewById<Button>(R.id.signup_button)
        val errorMessage = findViewById<TextView>(R.id.error_message)

        signupButton.setOnClickListener(){
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val city = cityEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            SupabaseClient.client.auth.signUp(email, password)
                .thenAccept { session ->
                    if(session != null){
                        SupabaseClient.client.from("users").insert(
                            mapOf(
                                "id" to session.user.id,
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "city" to city,
                                "email" to email,
                            )
                        ).thenAccept{
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        errorMessage.text = "Signup failed."
                    }
                }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}