package com.example.internetapi.ui.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.AccountOperationAdapterBinding
import com.example.internetapi.models.AccountOperation

class AccountOperationAdapter : RecyclerView.Adapter<AccountOperationsViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AccountOperation>() {
        override fun areItemsTheSame(
            oldItem: AccountOperation,
            newItem: AccountOperation
        ): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(
            oldItem: AccountOperation,
            newItem: AccountOperation
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<AccountOperation>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountOperationsViewHolder {

        val binding =
            AccountOperationAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return AccountOperationsViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: AccountOperationsViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            date.text = item.date
            value.text = MoneyFormatter.df.format(item.value)
            type.text = item.type
            if (item.type == "OUTCOME") {
                this.type.setTextColor(Color.RED)
            } else {
                this.type.setTextColor(Color.GREEN)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class AccountOperationsViewHolder(
    val binding: AccountOperationAdapterBinding,
    val parent: Context,
) :
    RecyclerView.ViewHolder(binding.root)
