package com.example.internetapi.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.SimpleAccount
import com.example.internetapi.ui.AccountOutcomeDetails
import com.example.internetapi.ui.InvoiceDetailsActivity


class AccountExpensesAdapter : RecyclerView.Adapter<AccountExpensesViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AccountInvoice>() {
        override fun areItemsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)
    private var account: Int = -1

    fun submitList(list: List<AccountInvoice>, accountId: Int) {
        account = accountId
        differ.submitList(list)
    }

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
            layout.setOnLongClickListener {
                createSpinner(holder, item)
                true
            }
            layout.setOnClickListener {

                val indent = Intent(holder.parent, InvoiceDetailsActivity::class.java).apply {
                    this.putExtra("invoiceId", item.listId.toLong())
                }
                ContextCompat.startActivity(holder.parent, indent, null)
            }
        }

    }

    private fun createSpinner(
        holder: AccountExpensesViewHolder,
        item: AccountInvoice
    ) {
        val accounts = AccountHolder.accounts
        val spinner = Spinner(holder.parent)
        spinner.adapter = ArrayAdapter(
            holder.parent,
            android.R.layout.simple_spinner_dropdown_item,
            accounts
        )
        spinner.setSelection(accounts.indexOfFirst { it.id == account })
        val alert: AlertDialog.Builder = AlertDialog.Builder(holder.parent)
        alert.setTitle("Change account for selected invoice")
            .setMessage("${item.listId}")
            .setView(spinner)
            .setPositiveButton("OK") { _, i ->
                Log.i(
                    "TAG",
                    "onBindViewHolder: OK ${(spinner.selectedItem as SimpleAccount).id}"
                )
                (holder.parent as AccountOutcomeDetails).updateInvoiceAccount(
                    item.listId,
                    account,
                    (spinner.selectedItem as SimpleAccount).id
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.i("TAG", "onBindViewHolder: CANCEL")
            }
        alert.show()
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun removeInvoice(invoiceId: Long) {
        differ.currentList.filter { it.listId != invoiceId }.let {
            submitList(it, account)
        }
    }
}

class AccountExpensesViewHolder(val binding: InvoiceAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
