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
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            
            // Configure X-axis with modern styling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(true)
                axisLineColor = context.getColor(R.color.divider_color)
                axisLineWidth = 1.5f
                granularity = 1f
                labelCount = 7
                textColor = context.getColor(R.color.text_secondary)
                textSize = 11f
                yOffset = 8f
            }
            
            // Configure Y-axis with centered zero
            axisLeft.apply {
                axisMinimum = -2.5f
                axisMaximum = 2.5f
                setDrawGridLines(true)
                gridColor = context.getColor(R.color.divider_color)
                gridLineWidth = 0.8f
                enableGridDashedLine(10f, 5f, 0f)
                setDrawAxisLine(false)
                setDrawZeroLine(true)
                zeroLineColor = context.getColor(R.color.text_tertiary)
                zeroLineWidth = 1.5f
                textColor = context.getColor(R.color.text_secondary)
                textSize = 10f
                setLabelCount(5, true)
            }
            
            axisRight.isEnabled = false
            
            // Configure legend with custom style
            legend.apply {
                isEnabled = true
                textColor = context.getColor(R.color.text_primary)
                textSize = 12f
                form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
                formSize = 10f
                xEntrySpace = 12f
                yEntrySpace = 4f
            }
            
            // Add extra offset for better spacing
            extraBottomOffset = 8f
            extraTopOffset = 12f
        }
        
        // Process mood data for the last 7 days
        val chartData = prepareMoodData(moodEntries)
        
        if (chartData.isNotEmpty()) {
            val dataSet = LineDataSet(chartData, "7-Day Mood Trend").apply {
                // Line styling with gradient effect
                color = context.getColor(R.color.accent_purple)
                lineWidth = 3.5f
                mode = LineDataSet.Mode.CUBIC_BEZIER  // Smooth curved lines
                cubicIntensity = 0.2f
                
                // Circle markers with custom design
                setCircleColor(context.getColor(R.color.accent_purple))
                circleRadius = 7f
                setDrawCircleHole(true)
                circleHoleRadius = 4f
                circleHoleColor = android.graphics.Color.WHITE
                
                // Value labels
                setDrawValues(false)  // Hide values for cleaner look
                
                // Gradient fill
                setDrawFilled(true)
                val gradientFill = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        context.getColor(R.color.accent_purple) and 0x4FFFFFFF.toInt(),
                        context.getColor(R.color.accent_purple) and 0x0FFFFFFF.toInt()
                    )
                )
                fillDrawable = gradientFill
                
                // Highlight styling
                highLightColor = context.getColor(R.color.primary)
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1.5f
                enableDashedHighlightLine(10f, 5f, 0f)
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
                // Convert 1-5 scale to -2 to +2 scale (0 in middle for better visualization)
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