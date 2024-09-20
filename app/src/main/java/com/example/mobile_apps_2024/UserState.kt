package com.example.mobile_apps_2024

sealed class UserState {
    object Loading : UserState()
    data class Success(val message: String) : UserState()
    data class Error(val message: String) : UserState()
}
