package com.example.SoulFlow.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.SoulFlow.R
import com.example.SoulFlow.data.models.MoodEntry
import com.example.SoulFlow.data.models.MoodType
import com.example.SoulFlow.data.repository.SharedPreferencesManager
import com.example.SoulFlow.ui.adapters.MoodSelectorAdapter
import com.example.SoulFlow.ui.adapters.MoodHistoryAdapter
import com.example.SoulFlow.ui.charts.MoodChartHelper
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.button.MaterialButton
import java.util.*

/**
 * Fragment for mood journaling with emoji selector
 */
class MoodFragment : Fragment() {
    
    private lateinit var recyclerMoodSelector: RecyclerView
    private lateinit var recyclerMoodHistory: RecyclerView
    private lateinit var btnSaveMood: MaterialButton
    private lateinit var btnShareMood: MaterialButton
    private lateinit var chartMoodTrend: LineChart
    
    private lateinit var prefsManager: SharedPreferencesManager
    private lateinit var moodSelectorAdapter: MoodSelectorAdapter
    private lateinit var moodHistoryAdapter: MoodHistoryAdapter
    private lateinit var moodChartHelper: MoodChartHelper
    
    private var selectedMood: MoodType? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize components
        initializeViews(view)
        setupMoodSelector()
        setupMoodHistory()
        setupClickListeners()
        loadMoodHistory()
    }
    
    override fun onResume() {
        super.onResume()
        loadMoodHistory()
    }
    
    private fun initializeViews(view: View) {
        prefsManager = SharedPreferencesManager.getInstance(requireContext())
        moodChartHelper = MoodChartHelper(requireContext())
        recyclerMoodSelector = view.findViewById(R.id.recycler_mood_selector)
        recyclerMoodHistory = view.findViewById(R.id.recycler_mood_history)
        btnSaveMood = view.findViewById(R.id.btn_save_mood)
        btnShareMood = view.findViewById(R.id.btn_share_mood)
        chartMoodTrend = view.findViewById(R.id.chart_mood_trend)
    }
    
    private fun setupMoodSelector() {
        moodSelectorAdapter = MoodSelectorAdapter { mood ->
            selectedMood = mood
            updateSaveButtonState()
        }
        
        recyclerMoodSelector.apply {
            layoutManager = GridLayoutManager(context, 5) // 5 columns for emojis
            adapter = moodSelectorAdapter
            setHasFixedSize(true)
        }
        
        // Load all available moods
        moodSelectorAdapter.updateMoods(MoodType.getAllMoods())
    }
    
    private fun setupMoodHistory() {
        moodHistoryAdapter = MoodHistoryAdapter(
            onDeleteClick = { entry -> deleteMoodEntry(entry) },
            onShareClick = { entry -> shareMoodEntry(entry) }
        )
        
        recyclerMoodHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodHistoryAdapter
            setHasFixedSize(true)
        }
    }
    
    
    private fun setupClickListeners() {
        btnSaveMood.setOnClickListener {
            saveMoodEntry()
        }
        
        btnShareMood.setOnClickListener {
            shareTodaysMood()
        }
    }
    
    private fun updateSaveButtonState() {
        btnSaveMood.isEnabled = selectedMood != null
    }
    
    private fun saveMoodEntry() {
        val mood = selectedMood ?: return
        
        val moodEntry = MoodEntry(
            mood = mood,
            emoji = mood.emoji,
            notes = "", // Removed notes field
            timestamp = Date()
        )
        
        prefsManager.saveMoodEntry(moodEntry)
        
        // Reset form
        selectedMood = null
        moodSelectorAdapter.clearSelection()
        updateSaveButtonState()
        
        // Refresh history
        loadMoodHistory()
        
        // Show success message
        android.widget.Toast.makeText(
            requireContext(),
            "Mood saved successfully!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun loadMoodHistory() {
        val moodEntries = prefsManager.getMoodEntries()
        moodHistoryAdapter.updateMoodEntries(moodEntries)
        
        // Update mood trend chart
        moodChartHelper.setupMoodTrendChart(chartMoodTrend, moodEntries)
        
        // Update share button state
        val todayEntries = prefsManager.getTodayMoodEntries()
        btnShareMood.isEnabled = todayEntries.isNotEmpty()
        
        // Update empty state visibility
        val isEmpty = moodEntries.isEmpty()
        recyclerMoodHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
        view?.findViewById<View>(R.id.layout_empty_mood_history)?.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
    
    private fun deleteMoodEntry(entry: MoodEntry) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                prefsManager.deleteMoodEntry(entry.id)
                loadMoodHistory()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun shareMoodEntry(entry: MoodEntry) {
        val shareText = "My mood: ${entry.mood.label} ${entry.emoji}"
        val formattedText = getString(R.string.share_mood_summary, shareText)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun shareTodaysMood() {
        val todayEntries = prefsManager.getTodayMoodEntries()
        if (todayEntries.isEmpty()) return
        
        val moodSummary = if (todayEntries.size == 1) {
            "${todayEntries.first().mood.label} ${todayEntries.first().emoji}"
        } else {
            val moods = todayEntries.joinToString(", ") { "${it.mood.label} ${it.emoji}" }
            "Multiple moods today: $moods"
        }
        
        val shareText = getString(R.string.share_mood_summary, moodSummary)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
}