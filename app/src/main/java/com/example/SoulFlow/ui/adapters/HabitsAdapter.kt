package com.example.SoulFlow.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.SoulFlow.R
import com.example.SoulFlow.data.models.Habit
import com.example.SoulFlow.data.models.HabitProgress
import com.google.android.material.button.MaterialButton

/**
 * Adapter for displaying habits in RecyclerView
 */
class HabitsAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onProgressClick: (Habit, HabitProgress) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onShareClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var habitsWithProgress: List<Pair<Habit, HabitProgress>> = emptyList()
    private lateinit var prefsManager: com.example.SoulFlow.data.repository.SharedPreferencesManager

    fun updateHabits(newHabitsWithProgress: List<Pair<Habit, HabitProgress>>) {
        habitsWithProgress = newHabitsWithProgress
        // Initialize SharedPreferences manager if not already done
        if (!::prefsManager.isInitialized && habitsWithProgress.isNotEmpty()) {
            // Get context from the first view holder when it's created
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val (habit, progress) = habitsWithProgress[position]
        holder.bind(habit, progress)
    }

    override fun getItemCount(): Int = habitsWithProgress.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHabitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val tvHabitDescription: TextView = itemView.findViewById(R.id.tv_habit_description)
        private val tvHabitTarget: TextView = itemView.findViewById(R.id.tv_habit_target)
        private val tvProgressText: TextView = itemView.findViewById(R.id.tv_progress_text)
        private val tvProgressPercentage: TextView = itemView.findViewById(R.id.tv_progress_percentage)
        private val tvStreak: TextView = itemView.findViewById(R.id.tv_streak)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar_habit)
        private val btnToggleCompletion: MaterialButton = itemView.findViewById(R.id.btn_toggle_completion)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btn_share)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(habit: Habit, progress: HabitProgress) {
            // Initialize SharedPreferences manager if needed
            if (!::prefsManager.isInitialized) {
                prefsManager = com.example.SoulFlow.data.repository.SharedPreferencesManager.getInstance(itemView.context)
            }
            
            // Basic habit info
            tvHabitName.text = habit.name
            
            if (habit.description.isNotBlank()) {
                tvHabitDescription.text = habit.description
                tvHabitDescription.visibility = View.VISIBLE
            } else {
                tvHabitDescription.visibility = View.GONE
            }
            
            tvHabitTarget.text = "Target: ${habit.targetValue} ${habit.unit}"

            // Progress info
            val progressPercentage = progress.getProgressPercentage(habit.targetValue)
            tvProgressText.text = "${progress.currentValue}/${habit.targetValue} ${habit.unit}"
            tvProgressPercentage.text = "$progressPercentage%"
            progressBar.progress = progressPercentage

            // Streak info with fire emoji
            val streak = prefsManager.calculateHabitStreak(habit.id)
            tvStreak.text = "🔥 $streak days streak"

            // Completion button
            if (progress.isCompleted) {
                btnToggleCompletion.text = itemView.context.getString(R.string.mark_incomplete)
                btnToggleCompletion.setBackgroundColor(
                    itemView.context.getColor(R.color.primary)
                )
                btnToggleCompletion.setTextColor(
                    itemView.context.getColor(R.color.white)
                )
            } else {
                btnToggleCompletion.text = itemView.context.getString(R.string.mark_complete)
                btnToggleCompletion.setBackgroundColor(
                    itemView.context.getColor(R.color.switch_track_checked)
                )
                btnToggleCompletion.setTextColor(
                    itemView.context.getColor(R.color.primary)
                )
            }

            // Click listeners
            itemView.setOnClickListener { onHabitClick(habit) }
            btnToggleCompletion.setOnClickListener { onProgressClick(habit, progress) }
            btnShare.setOnClickListener { onShareClick(habit) }
            btnDelete.setOnClickListener { onDeleteClick(habit) }
            
            // Update progress bar color based on completion
            val progressColor = if (progress.isCompleted) {
                itemView.context.getColor(R.color.primary)
            } else {
                itemView.context.getColor(R.color.primary)
            }
            progressBar.progressTintList = android.content.res.ColorStateList.valueOf(progressColor)
        }
    }
}