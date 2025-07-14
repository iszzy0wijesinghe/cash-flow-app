package com.example.money.ui.goals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.money.R
import com.example.money.data.CurrencyManager
import com.example.money.models.Goal
import com.example.money.models.GoalType
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Locale

class GoalAdapter(
    private val goals: List<Goal>,
    private val onGoalClick: (Goal) -> Unit,
    private val currencyManager: CurrencyManager
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val goalName: TextView = view.findViewById(R.id.textGoalName)
        val goalType: TextView = view.findViewById(R.id.textGoalType)
        val targetAmount: TextView = view.findViewById(R.id.textTargetAmount)
        val targetDate: TextView = view.findViewById(R.id.textTargetDate)
        val progressIndicator: LinearProgressIndicator = view.findViewById(R.id.progressIndicator)
        val progressText: TextView = view.findViewById(R.id.textProgress)
        val reward: TextView = view.findViewById(R.id.textReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        
        holder.goalName.text = goal.name
        holder.goalType.text = when (goal.type) {
            GoalType.SAVINGS -> "Savings Goal"
            GoalType.DEBT_REPAYMENT -> "Debt Repayment"
            GoalType.INVESTMENT -> "Investment"
        }
        
        holder.targetAmount.text = currencyManager.formatAmount(goal.targetAmount)
        holder.targetDate.text = dateFormat.format(goal.targetDate)
        
        holder.progressIndicator.progress = goal.progress
        holder.progressText.text = "${goal.progress}% Complete"
        
        holder.reward.text = "Reward: ${goal.reward}"
        
        holder.itemView.setOnClickListener {
            onGoalClick(goal)
        }
    }

    override fun getItemCount() = goals.size
} 