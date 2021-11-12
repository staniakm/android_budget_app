package com.example.internetapi.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.ui.InvoiceDetailsActivity
import java.text.DecimalFormat


class AccountExpensesAdapter : RecyclerView.Adapter<AccountExpensesViewHolder>() {

    var df: DecimalFormat = DecimalFormat("##0.00")

    private val diffCallback = object : DiffUtil.ItemCallback<AccountInvoice>() {
        override fun areItemsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<AccountInvoice>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountExpensesViewHolder {

        val binding =
            InvoiceAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountExpensesViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: AccountExpensesViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            shopName.text = item.name
            cost.text = df.format(item.price)
            date.text = item.date
            layout.setOnClickListener {
                val indent = Intent(holder.parent, InvoiceDetailsActivity::class.java).apply {
                    this.putExtra("invoiceId", item.listId.toLong())
                }
                ContextCompat.startActivity(holder.parent, indent, null)
            }
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class AccountExpensesViewHolder(val binding: InvoiceAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
