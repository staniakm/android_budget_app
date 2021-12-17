package com.example.internetapi.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.LayoutAdapterBinding
import com.example.internetapi.models.Account
import com.example.internetapi.ui.AccountActivity
import com.example.internetapi.ui.AccountDetailsActivity
import com.example.internetapi.ui.AccountUpdateActivity
import java.math.BigDecimal

class AccountAdapter(private val accountActivity: AppCompatActivity) : RecyclerView.Adapter<AccountViewHolder>() {

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

    fun updateListItem(accountId: Int, newName: String, newValue: BigDecimal) {
        differ.currentList.map {
            if (it.id == accountId)
                it.copy(name = newName, moneyAmount = newValue)
            else it
        }.let {
            differ.submitList(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {

        val binding =
            LayoutAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.editBtn.setOnClickListener {
            Log.i("Account Adapter", "onBindViewHolder: ")
            val indent = Intent(holder.parent, AccountUpdateActivity::class.java).apply {
                this.putExtra("account", item)
            }
            //FIXME switch to new api
            accountActivity.startActivityForResult(indent, 123)
        }
        holder.binding.apply {
            accName.text = item.name
            accIncome.text = "Przychód: ${df.format(item.income)}"
            accExpense.text = "Wydatki: ${df.format(item.expense)}"
            accCurrentBalance.text = "Stan konta: ${df.format(item.moneyAmount)}"
            layout.setOnClickListener {
                val indent = Intent(holder.parent, AccountDetailsActivity::class.java).apply {
                    this.putExtra("accountId", item.id.toLong())
                }
                startActivity(holder.parent, indent, null)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class AccountViewHolder(val binding: LayoutAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
