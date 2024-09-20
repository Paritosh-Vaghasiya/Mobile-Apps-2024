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
                // Attempt to sign up the user using the provided email and password
                val response = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                // If the signup response is successful
                if (response != null) {
                    val data = mapOf(
                        "firstname" to firstName,
                        "lastname" to lastName,
                        "city" to city,
                        "email" to email
                    )

                    // Insert the user's data into the 'users' table
                    val insertResponse = SupabaseClient.client.from("users").insert(data)
                    println("Insert Response: $insertResponse")

                    withContext(Dispatchers.Main) {
                        try {
                            // Handle the insert result if it's successful
                            if (insertResponse != null) {
                                userState.value = UserState.Success("Signup successful!")
                            } else {
                                // If insertResponse is null or failed, show an error
                                userState.value = UserState.Error("Failed to save user data.")
                            }
                        } catch (e: Exception) {
                            // Catch and handle any exceptions during the insert
                            userState.value = UserState.Error("Error saving user data: ${e.message}")
                            e.printStackTrace() // Print the error for debugging
                        }
                    }
                }
            } catch (e: Exception) {
                // Catch and handle any exceptions during signup
                withContext(Dispatchers.Main) {
                    userState.value = UserState.Error(e.message ?: "Unknown error occurred during signup")
                    e.printStackTrace() // Print the error for debugging
                }
            }
        }
    }
}