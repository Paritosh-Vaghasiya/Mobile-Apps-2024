package com.example.mobile_apps_2024

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobile_apps_2024.R
import com.example.mobile_apps_2024.SupabaseClient
import com.example.mobile_apps_2024.UserState
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class LoginActivity : AppCompatActivity() {

    private val userState = MutableLiveData<UserState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val errorMessage = findViewById<TextView>(R.id.error_message)
        val signupButton = findViewById<Button>(R.id.signup_button)

        // Observe changes in userState
        userState.observe(this, Observer { state ->
            when (state) {
                is UserState.Loading -> {
                    // Show loading indicator
                    Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                }
                is UserState.Success -> {
                    // Handle success, show message, navigate to next screen
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, Display::class.java))
                    finish()
                }
                is UserState.Error -> {
                    // Handle error, display error message
                    errorMessage.text = state.message
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, Signup::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        CoroutineScope(Dispatchers.Main).launch {
            userState.value = UserState.Loading // Set state to loading

            try {
                val response = SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                if (response != null) {
                    userState.value = UserState.Success("Login successful!")
                } else {
                    userState.value = UserState.Error("Login failed: {e.message}")
                }
            } catch (e: Exception) {
                userState.value = UserState.Error(e.message ?: "Unknown error")
            }
        }
    }
}