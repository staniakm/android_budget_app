package com.example.internetapi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.BudgetAdapterBinding
import com.example.internetapi.models.MonthBudget
import java.text.DecimalFormat


class MonthBudgetAdapter : RecyclerView.Adapter<MonthBudgetViewHolder>() {

    var df: DecimalFormat = DecimalFormat("##0.00")

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

        return MonthBudgetViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: MonthBudgetViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            budgetName.text = item.category
            spend.text = "Wyd: ${df.format(item.spent)}"
            planed.text = "Zap: ${df.format(item.planned)}"
            percentage.text = "${item.percentage}%"
            progressBar.setProgress(item.percentage, false)
            if (item.spent > item.planned) {
                this.spend.setTextColor(Color.RED)
                progressBar.progressDrawable.setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                this.spend.setTextColor(Color.GREEN)
                progressBar.progressDrawable.setColorFilter(
                    Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class MonthBudgetViewHolder(val binding: BudgetAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
