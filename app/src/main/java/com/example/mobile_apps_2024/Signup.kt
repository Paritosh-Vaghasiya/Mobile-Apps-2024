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
import com.example.mobile_apps_2024.SupabaseClient.client
import com.example.mobile_apps_2024.UserState
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.admin.AdminUserUpdateBuilder
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Signup : AppCompatActivity() {

    private val userState = MutableLiveData<UserState>()

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

        // Observe changes in userState
        userState.observe(this, Observer { state ->
            when (state) {
                is UserState.Loading -> {
                    // Show loading indicator
                    Toast.makeText(this, "Signing up...", Toast.LENGTH_SHORT).show()
                }
                is UserState.Success -> {
                    // Handle success, show message, navigate to login screen
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                is UserState.Error -> {
                    // Handle error, display error message
                    errorMessage.text = state.message
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        })

        signupButton.setOnClickListener() {
            val firstName = firstNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val city = cityEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || city.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                signupUser(firstName, lastName, email, city, password)
            }
        }
    }

    private fun signupUser(firstName: String, lastName: String, email: String, city: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userState.postValue(UserState.Loading) // Set state to loading

            try {
                // Sign up user using email and password
                val signUpResult = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                // Proceed to insert user details into 'Table_1' after sign-up
                if (signUpResult == null) {  // Check if user is returned in signUpResult
                    val data = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "city" to city,
                        "email" to email
                    )

                    // Insert user data into 'Table_1'
                    try {
                        val insertResponse = SupabaseClient.client.from("Table_1").insert(data)

                        withContext(Dispatchers.Main) {
                            if (insertResponse != null) {
                                userState.value = UserState.Success("Signup and data insertion successful!")
                            } else {
                                userState.value = UserState.Error("Failed to save user data.")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            userState.value = UserState.Error("Error inserting data: ${e.message}")
                        }
                    }
                } else {
                    // Handle sign-up failure
                    withContext(Dispatchers.Main) {
                        userState.value = UserState.Error("Signup failed. User not created.")
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions (e.g., network or Supabase errors)
                withContext(Dispatchers.Main) {
                    userState.value = UserState.Error(e.message ?: "Unknown error occurred during signup")
                    e.printStackTrace() // Log the error for debugging
                }
            }
        }
    }

}