package com.example.mobile_apps_2024

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.withContext

class Display : AppCompatActivity() {

    private lateinit var  displayText:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)

        displayText = findViewById(R.id.display)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        fetchUserData()

        logoutButton.setOnClickListener{
            logoutUser()
        }
    }

    private fun logoutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = SupabaseClient.client.auth.signOut()
                withContext(Dispatchers.Main){
                    if(response.isSuccessful){
                        startActivity(Intent(this@Display, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Display, "Logout Failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
                    Toast.makeText(this@Display, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchUserData() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = SupabaseClient.client.from("users").select(columns = Columns.list("email")) {
                            filter {
                                user.email?.let { eq ("email", it)}
                            }
                        }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val userData = response.data.get(0)
                            val userText = "Welcome ${userData.get("firstname")} ${userData.get("lastname")} from ${userData.get("city")}"
                            displayText.text = userText
                        } else {
                            Toast.makeText(this@Display, "Failed to fetch user data: ${response.error?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@Display, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}