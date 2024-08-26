package com.example.mobile_apps_2024

import io.supabase.client.SupabaseClient

object SupabaseClient {
    private const val SUPABASE_URL = "https://egzhuriimugvkjiauphl.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVnemh1cmlpbXVndmtqaWF1cGhsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjQwNzEzNjcsImV4cCI6MjAzOTY0NzM2N30.29e4s0hYCEB3e4m0GDB2WgSpEDbiJSSC4FOg5aU8ZOk"

    val client = SupabaseClient.create(SUPABASE_URL, SUPABASE_KEY)
}