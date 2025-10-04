package com.example.SoulFlow.ui.charts

import android.content.Context
import com.example.SoulFlow.R
import com.example.SoulFlow.data.models.MoodEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for creating mood trend charts
 */
class MoodChartHelper(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    
    fun setupMoodTrendChart(chart: LineChart, moodEntries: List<MoodEntry>) {
        chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(android.graphics.Color.WHITE)
            
            // Configure X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = 7
                textColor = context.getColor(R.color.text_secondary)
            }
            
            // Configure Y-axis to have 0 in the middle
            axisLeft.apply {
                axisMinimum = -3f  // Changed to allow negative values
                axisMaximum = 3f   // Changed to allow positive values
                setDrawGridLines(true)
                textColor = context.getColor(R.color.text_secondary)
            }
            
            axisRight.isEnabled = false
            
            // Configure legend
            legend.apply {
                isEnabled = true
                textColor = context.getColor(R.color.text_primary)
            }
        }
        
        // Process mood data for the last 7 days
        val chartData = prepareMoodData(moodEntries)
        
        if (chartData.isNotEmpty()) {
            val dataSet = LineDataSet(chartData, "Daily Mood").apply {
                color = context.getColor(R.color.primary)
                setCircleColor(context.getColor(R.color.primary))
                lineWidth = 3f
                circleRadius = 6f
                setDrawCircleHole(false)
                valueTextSize = 12f
                valueTextColor = context.getColor(R.color.text_primary)
                setDrawFilled(true)
                fillColor = context.getColor(R.color.switch_track_checked)
                fillAlpha = 50
            }
            
            val lineData = LineData(dataSet)
            chart.data = lineData
            
            // Set up X-axis labels
            val labels = getLast7DaysLabels()
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            
            chart.invalidate() // Refresh chart
        } else {
            chart.clear()
            chart.invalidate()
        }
    }
    
    private fun prepareMoodData(moodEntries: List<MoodEntry>): List<Entry> {
        val calendar = Calendar.getInstance()
        val entries = mutableListOf<Entry>()
        
        // Get mood data for the last 7 days
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            
            // Find moods for this day
            val dayMoods = moodEntries.filter { it.date == dateString }
            
            if (dayMoods.isNotEmpty()) {
                // Calculate average mood for the day and center around 0
                // Convert 1-5 scale to -2 to +2 scale (0 in middle)
                val averageMood = dayMoods.map { it.mood.value }.average().toFloat()
                val centeredMood = averageMood - 3f  // Shift from 1-5 to -2 to +2
                entries.add(Entry((6 - i).toFloat(), centeredMood))
            } else {
                // No mood entry for this day
                entries.add(Entry((6 - i).toFloat(), 0f)) // 0 for no data
            }
        }
        
        return entries
    }
    
    private fun getLast7DaysLabels(): List<String> {
        val calendar = Calendar.getInstance()
        val labels = mutableListOf<String>()
        
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            labels.add(dayFormat.format(calendar.time))
        }
        
        return labels
    }
}