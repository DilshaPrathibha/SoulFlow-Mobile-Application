package com.example.SoulFlow.onboards

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.SoulFlow.MainActivity
import com.example.SoulFlow.R
import com.example.SoulFlow.data.repository.SharedPreferencesManager

class Launching : AppCompatActivity() {
    
    private lateinit var prefsManager: SharedPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_launching)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SharedPreferencesManager
        prefsManager = SharedPreferencesManager.getInstance(this)
        
        // Navigate after 3 seconds based on login status
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 3000)
    }
    
    private fun navigateToNextScreen() {
        val intent = if (prefsManager.isUserLoggedIn()) {
            // User is logged in, go directly to MainActivity
            Intent(this, MainActivity::class.java)
        } else {
            // User is not logged in, show onboarding
            Intent(this, Onboard1::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}