package com.example.internetapi.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.IncomeAdapterBinding
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.ui.InvoiceDetailsActivity
import java.text.DecimalFormat
import com.example.internetapi.config.MoneyFormatter.df

class AccountIncomesAdapter : RecyclerView.Adapter<AccountIncomesViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AccountIncome>() {
        override fun areItemsTheSame(oldItem: AccountIncome, newItem: AccountIncome): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountIncome, newItem: AccountIncome): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<AccountIncome>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountIncomesViewHolder {

        val binding =
            IncomeAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountIncomesViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: AccountIncomesViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            description.text = item.description
            amount.text = df.format(item.income)
            date.text = item.date
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class AccountIncomesViewHolder(val binding: IncomeAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
