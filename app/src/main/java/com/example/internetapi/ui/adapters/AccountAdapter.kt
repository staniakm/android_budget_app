package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.AccountAdapterBinding
import com.example.internetapi.models.Account
import com.example.internetapi.models.UpdateAccountResponse

class AccountAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<AccountViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Account>) = differ.submitList(list)

    fun updateListItem(account: UpdateAccountResponse) {
        differ.currentList.map {
            if (it.id == account.id.toInt())
                it.copy(name = account.name, moneyAmount = account.amount)
            else it
        }.let {
            differ.submitList(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {

        val binding =
            AccountAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            accName.text = item.name
            accIncome.text = "Przychód: ${df.format(item.income)}"
            accExpense.text = "Wydatki: ${df.format(item.expense)}"
            accCurrentBalance.text = "Stan konta: ${df.format(item.moneyAmount)}"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getItem(position: Int): Account = differ.currentList[position]
}

class AccountViewHolder(
    val binding: AccountAdapterBinding,
    val parent: Context,
    private val listener: OnItemClickedListener
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private val layout = binding.layout
    private val editButton = binding.editBtn

    init {
        layout.setOnClickListener(this)
        editButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            layout.id -> adapterPosition.let {
                if (it != RecyclerView.NO_POSITION) {
                    listener.onClick(it, "layout")
                }
            }
            editButton.id -> adapterPosition.let {
                listener.onClick(it, "edit")
            }
        }
    }
}
