package com.example.internetapi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.BudgetAdapterBinding
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.UpdateBudgetResponse

class MonthBudgetAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<MonthBudgetViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<MonthBudget>() {
        override fun areItemsTheSame(oldItem: MonthBudget, newItem: MonthBudget): Boolean {
            return oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: MonthBudget, newItem: MonthBudget): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<MonthBudget>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthBudgetViewHolder {

        val binding =
            BudgetAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MonthBudgetViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: MonthBudgetViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            composeBudgetCardSummary.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    MaterialTheme {
                        BudgetSummaryCard(item)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun updateBudget(budget: UpdateBudgetResponse) {
        differ.currentList.map {
            if (it.budgetId == budget.budgetId) it.copy(
                planned = budget.planned,
                percentage = budget.percentage
            ) else it
        }.let {
            differ.submitList(it)
        }
    }

    fun getItem(position: Int): MonthBudget = differ.currentList[position]
}

class MonthBudgetViewHolder(
    val binding: BudgetAdapterBinding,
    val parent: Context,
    private val listener: OnItemClickedListener
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private val layout = binding.layout

    init {
        layout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            layout.id -> bindingAdapterPosition.let {
                if (it != RecyclerView.NO_POSITION) {
                    listener.onClick(it, "layout")
                }
            }
        }
    }
}
