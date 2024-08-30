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
import androidx.compose.ui.text.input.KeyboardType.Companion.Email
import com.example.mobile_apps_2024.SupabaseClient.client
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

            if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || city.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
            } else {
                signupUser(firstName,lastName,email,city,password)
            }
        }
    }

    private fun signupUser(firstName: String, lastName: String, email: String, city: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = SupabaseClient.client.auth.signUpWith(Email){

                }
                if (response.isSuccessful){
                    val user = response.user
                    val data = mapOf(
                        "firstname" to firstName,
                        "lastname" to lastName,
                        "city" to city,
                        "email" to email,
                    )
                val insertResponse = SupabaseClient.client.from("users").insert(data).execute()
                    withContext(Dispatchers.Main){
                        if(insertResponse.isSuccessful){
                            startActivity(Intent(this@Signup, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@Signup,"Failed to save user data: ${insertResponse.error?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Signup,"Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}