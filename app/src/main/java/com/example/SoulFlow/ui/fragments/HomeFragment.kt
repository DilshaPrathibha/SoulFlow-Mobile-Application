package com.example.SoulFlow.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.SoulFlow.R
import com.example.SoulFlow.data.repository.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home fragment - Main landing page
 */
class HomeFragment : Fragment() {
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var tvWelcome: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvHabitsSummary: TextView
    private lateinit var tvMoodSummary: TextView
    private lateinit var tvHydrationSummary: TextView
    private lateinit var cardHabits: CardView
    private lateinit var cardMood: CardView
    private lateinit var cardHydration: CardView
    private lateinit var tvTotalHabits: TextView
    private lateinit var tvTotalMoods: TextView
    private lateinit var tvStreakDays: TextView
    private lateinit var tvDailyTip: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        
        // Initialize views
        tvWelcome = view.findViewById(R.id.tv_welcome)
        tvDate = view.findViewById(R.id.tv_date)
        tvHabitsSummary = view.findViewById(R.id.tv_habits_summary)
        tvMoodSummary = view.findViewById(R.id.tv_mood_summary)
        tvHydrationSummary = view.findViewById(R.id.tv_hydration_summary)
        cardHabits = view.findViewById(R.id.card_habits)
        cardMood = view.findViewById(R.id.card_mood)
        cardHydration = view.findViewById(R.id.card_hydration)
        tvTotalHabits = view.findViewById(R.id.tv_total_habits)
        tvTotalMoods = view.findViewById(R.id.tv_total_moods)
        tvStreakDays = view.findViewById(R.id.tv_streak_days)
        tvDailyTip = view.findViewById(R.id.tv_daily_tip)
        
        setupWelcomeMessage()
        setupDateDisplay()
        loadTodaysSummary()
        setupCardClickListeners()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when user comes back to home
        loadTodaysSummary()
    }
    
    private fun setupWelcomeMessage() {
        val userName = prefsManager.getUserName()
        tvWelcome.text = if (userName.isNotEmpty()) {
            "Welcome back, $userName!"
        } else {
            "Welcome back!"
        }
    }
    
    private fun setupDateDisplay() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(Date())
    }
    
    private fun loadTodaysSummary() {
        loadHabitsSummary()
        loadMoodSummary()
        loadHydrationSummary()
        loadWeeklyStats()
        loadDailyTip()
    }
    
    private fun loadHabitsSummary() {
        val today = prefsManager.getCurrentDateString()
        val allHabits = prefsManager.getHabits().filter { it.isActive }
        val todayProgress = prefsManager.getHabitProgress().filter { it.date == today }
        val completedCount = todayProgress.count { it.isCompleted }
        val totalCount = allHabits.size
        
        tvHabitsSummary.text = if (totalCount > 0) {
            "$completedCount of $totalCount habits completed"
        } else {
            getString(R.string.no_habits)
        }
    }
    
    private fun loadMoodSummary() {
        val today = prefsManager.getCurrentDateString()
        val todayMoods = prefsManager.getMoodEntries().filter { it.date == today }
        
        tvMoodSummary.text = if (todayMoods.isNotEmpty()) {
            val latestMood = todayMoods.last()
            "Feeling ${latestMood.mood.label} ${latestMood.emoji}"
        } else {
            getString(R.string.no_mood)
        }
    }
    
    private fun loadHydrationSummary() {
        val today = prefsManager.getCurrentDateString()
        val todayIntake = prefsManager.getHydrationIntake()
            .filter { it.date == today }
            .sumOf { it.amountMl }
        val goal = prefsManager.getHydrationSettings().dailyGoalMl
        
        tvHydrationSummary.text = if (todayIntake > 0) {
            "${todayIntake}ml of ${goal}ml"
        } else {
            getString(R.string.no_hydration)
        }
    }
    
    private fun setupCardClickListeners() {
        // Navigate to respective fragments when cards are clicked
        cardHabits.setOnClickListener {
            navigateToFragment(R.id.nav_habits)
        }
        
        cardMood.setOnClickListener {
            navigateToFragment(R.id.nav_mood)
        }
        
        cardHydration.setOnClickListener {
            navigateToFragment(R.id.nav_hydration)
        }
    }
    
    private fun navigateToFragment(itemId: Int) {
        activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = itemId
    }
    
    private fun loadWeeklyStats() {
        // Calculate total active habits
        val totalHabits = prefsManager.getHabits().count { it.isActive }
        tvTotalHabits.text = totalHabits.toString()
        
        // Calculate total mood entries this week
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val weekAgo = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(calendar.time)
        val today = prefsManager.getCurrentDateString()
        val weeklyMoods = prefsManager.getMoodEntries().count { 
            it.date >= weekAgo && it.date <= today 
        }
        tvTotalMoods.text = weeklyMoods.toString()
        
        // Calculate best streak from all habits
        val habits = prefsManager.getHabits().filter { it.isActive }
        val maxStreak = if (habits.isNotEmpty()) {
            habits.maxOfOrNull { habit ->
                prefsManager.calculateHabitStreak(habit.id)
            } ?: 0
        } else {
            0
        }
        tvStreakDays.text = maxStreak.toString()
    }
    
    private fun loadDailyTip() {
        val tips = listOf(
            "Consistency is key! Small daily actions lead to big changes over time.",
            "Drink water first thing in the morning to kickstart your metabolism.",
            "Track your mood regularly to identify patterns and triggers.",
            "Celebrate small wins! Every completed habit is a step forward.",
            "Don't break the chain! Even on tough days, do the minimum.",
            "Quality sleep improves mood, focus, and habit formation.",
            "Set realistic goals and gradually increase difficulty."
        )
        
        // Show different tip based on day of week
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        val tipIndex = (dayOfWeek - 1) % tips.size
        tvDailyTip.text = tips[tipIndex]
    }
}
