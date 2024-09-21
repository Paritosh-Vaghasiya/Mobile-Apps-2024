package com.example.mobile_apps_2024

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mobile_apps_2024.SupabaseClient
import com.example.mobile_apps_2024.UserState
import com.example.mobile_apps_2024.User
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

class Display : AppCompatActivity() {

    private lateinit var displayText: TextView
    private val userState = MutableLiveData<UserState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display)

        displayText = findViewById(R.id.display)
        val logoutButton = findViewById<Button>(R.id.logout_button)
        val errorMessage = findViewById<TextView>(R.id.error_message)

        // Observe changes in userState
        userState.observe(this, Observer { state ->
            when (state) {
                is UserState.Loading -> {
                    Toast.makeText(this, "Loading...", Toast.LENGTH_LONG).show()
                }
                is UserState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is UserState.Error -> {
                    errorMessage.text = state.message
                    Toast.makeText(this, "Error: $state.message", Toast.LENGTH_LONG).show()
                }
            }
        })

        fetchUserData()

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            userState.postValue(UserState.Loading) // Set state to Loading before the operation

            try {
                val response = SupabaseClient.client.auth.signOut()
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        userState.value = UserState.Success("Logged out successfully!")
                        startActivity(Intent(this@Display, Logout::class.java))
                        finish()
                    } else {
                        userState.value = UserState.Error("Logout Failed: (e.message)")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    userState.value = UserState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    @Serializable
    data class User(
        @SerialName("firstName") val firstname: String,
        @SerialName("lastName") val lastname: String,
        @SerialName("city") val city: String
    )

    private fun fetchUserData() {
        val user = SupabaseClient.client.auth.currentUserOrNull()
        if (user != null) {
            CoroutineScope(Dispatchers.IO).launch {
                userState.postValue(UserState.Loading) // Set state to Loading before the operation
                try {
                    // Fetch data from Supabase using a suspending function (no execute() required)
                    val response = SupabaseClient.client
                        .from("Table_1")
                        .select(columns = Columns.list("firstName", "lastName", "city")) {
                            filter { eq("email", user.email ?: "") }
                        }
                        .decodeList<User>() // Decodes the response into a list of `User` objects
                    println(response.toString())

                    withContext(Dispatchers.Main) {
                        if (response.isNotEmpty()) {
                            val fetchedUser = response[0] // Assuming one user is fetched based on email
                            val userText = "Welcome ${fetchedUser.firstname} ${fetchedUser.lastname} from ${fetchedUser.city}"
                            displayText.text = userText
                            userState.value = UserState.Success("User data fetched successfully!")
                        } else {
                            userState.value = UserState.Error("No user data found ?(e.message)")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        userState.value = UserState.Error(e.message ?: "Unknown error")
                    }
                }
            }
        } else {
            userState.postValue(UserState.Error("No user logged in"))
        }
    }
}