package com.example.SoulFlow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.SoulFlow.data.repository.SharedPreferencesManager
import com.example.SoulFlow.receivers.HydrationAlarmScheduler
import com.example.SoulFlow.sensors.SensorManager
import com.example.SoulFlow.ui.auth.LoginActivity
import com.example.SoulFlow.ui.fragments.HomeFragment
import com.example.SoulFlow.ui.fragments.HabitsFragment
import com.example.SoulFlow.ui.fragments.HydrationFragment
import com.example.SoulFlow.ui.fragments.MoodFragment
import com.example.SoulFlow.ui.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuButton: FloatingActionButton
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var sensorManager: SensorManager
    
    // Permission request code
    private val PERMISSION_REQUEST_CODE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is logged in
        prefsManager = SharedPreferencesManager.getInstance(this)
        if (!prefsManager.isUserLoggedIn()) {
            // Redirect to login screen
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        setContentView(R.layout.activity_main)
        
        // Initialize views
        bottomNavigation = findViewById(R.id.bottom_navigation)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        menuButton = findViewById(R.id.menu_button)
        
        // Setup navigation
        setupBottomNavigation()
        setupDrawer()
        
        // Set up window insets for proper layout handling
        setupWindowInsets()
        
        // Setup shake detection
        setupShakeDetection()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        
        // Check and request notification permissions on app start
        checkAndRequestNotificationPermission()
        
        // Check and request exact alarm permissions on app start
        checkAndRequestExactAlarmPermission()
        
        // Handle intent extras
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        sensorManager.startListening()
    }
    
    override fun onPause() {
        super.onPause()
        sensorManager.stopListening()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodFragment()
                R.id.nav_hydration -> HydrationFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
        
        // Set the default selected item
        bottomNavigation.selectedItemId = R.id.nav_home
    }
    
    private fun setupDrawer() {
        // Setup menu button to open drawer
        menuButton.setOnClickListener {
            drawerLayout.open()
        }
        
        // Set logout item icon color to red
        val logoutItem = navigationView.menu.findItem(R.id.nav_logout)
        logoutItem?.let {
            it.icon?.setTint(ContextCompat.getColor(this, R.color.error_red))
        }
        
        // Setup navigation view item selection
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                // Main Navigation
                R.id.nav_drawer_home -> {
                    bottomNavigation.selectedItemId = R.id.nav_home
                    loadFragment(HomeFragment())
                }
                R.id.nav_drawer_habits -> {
                    bottomNavigation.selectedItemId = R.id.nav_habits
                    loadFragment(HabitsFragment())
                }
                R.id.nav_drawer_mood -> {
                    bottomNavigation.selectedItemId = R.id.nav_mood
                    loadFragment(MoodFragment())
                }
                R.id.nav_drawer_hydration -> {
                    bottomNavigation.selectedItemId = R.id.nav_hydration
                    loadFragment(HydrationFragment())
                }
                
                // Settings & Preferences
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    // Unselect all bottom navigation items
                    bottomNavigation.menu.findItem(bottomNavigation.selectedItemId)?.isChecked = false
                }
                
                // Information & Help
                R.id.nav_about -> {
                    showAboutDialog()
                }
                R.id.nav_share -> {
                    shareApp()
                }
                
                // Account
                R.id.nav_logout -> {
                    showLogoutConfirmation()
                }
            }
            drawerLayout.close()
            true
        }
    }
    
    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.about))
            .setMessage(getString(R.string.about_description))
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out SoulFlow!")
            putExtra(
                Intent.EXTRA_TEXT,
                "Track your wellness journey with SoulFlow - Habits, Mood & Hydration tracker!\n\nDownload now: https://play.google.com/store/"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share SoulFlow via"))
    }
    
    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        // Clear user login status
        prefsManager.setUserLoggedIn(false)
        
        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    
    private fun setupShakeDetection() {
        sensorManager = SensorManager(this)
        sensorManager.setOnShakeDetectedListener {
            // Navigate to mood page when shake is detected
            runOnUiThread {
                bottomNavigation.selectedItemId = R.id.nav_mood
                loadFragment(MoodFragment())
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
    
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Show explanation dialog before requesting permission
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Notification Permission Required")
                    .setMessage("SoulFlow needs notification permission to send you hydration reminders. Please grant this permission to receive timely reminders.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    private fun checkAndRequestExactAlarmPermission() {
        // Only check on Android 12+ (API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!HydrationAlarmScheduler.canScheduleExactAlarms(this)) {
                // Show explanation dialog before requesting permission
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Exact Alarm Permission Required")
                    .setMessage("SoulFlow needs permission to schedule exact alarms for precise hydration reminders. Please grant this permission to receive timely reminders.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        HydrationAlarmScheduler.requestExactAlarmPermission(this)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(
                        this,
                        "Notification permission granted. You'll now receive hydration reminders!",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Show explanation why permission is needed
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Without notification permission, you won't receive hydration reminders. You can enable this permission later in Settings > Apps > SoulFlow > Permissions.")
                        .setPositiveButton("Open Settings") { _, _ ->
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            )
                            val uri = android.net.Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
    
    // Method to update the toolbar title from fragments (no longer needed)
    /*
    fun updateToolbarTitle(title: String) {
        findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.title = title
    }
    */
    
    private fun handleIntent(intent: Intent) {
        when {
            intent.getBooleanExtra("open_hydration", false) -> {
                bottomNavigation.selectedItemId = R.id.nav_hydration
                loadFragment(HydrationFragment())
                
                // Handle quick add water from notification
                if (intent.getBooleanExtra("quick_add_water", false)) {
                    // Add default amount of water (250ml)
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val intake = com.example.SoulFlow.data.models.HydrationIntake(
                        date = today,
                        amountMl = 250,
                        timestamp = java.util.Date()
                    )
                    prefsManager.addHydrationIntake(intake)
                    
                    android.widget.Toast.makeText(
                        this,
                        "Added 250ml of water",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                // Default to home fragment
                loadFragment(HomeFragment())
            }
        }
    }
}