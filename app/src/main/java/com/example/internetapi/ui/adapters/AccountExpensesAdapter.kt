package com.example.internetapi.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.SimpleAccount
import com.example.internetapi.ui.AccountDetailsActivity


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
            val accounts = arrayOf(
                SimpleAccount(1, "Account 1"),
                SimpleAccount(2, "Account 2"),
                SimpleAccount(3, "Account 3"),
            )
            layout.setOnClickListener {
                val spinner = Spinner(holder.parent)
                spinner.adapter = ArrayAdapter(
                    holder.parent,
                    android.R.layout.simple_spinner_dropdown_item,
                    accounts
                )
                spinner.setSelection(accounts.indexOfFirst { it.id == account })
                val alert: AlertDialog.Builder = AlertDialog.Builder(holder.parent)
                alert.setTitle("Some alert dialog")
                    .setMessage("Some message")
                    .setView(spinner)
                    .setPositiveButton("OK") { dialogInterface, i ->
                        Log.i(
                            "TAG",
                            "onBindViewHolder: OK ${(spinner.selectedItem as SimpleAccount).id}"
                        )
                        (holder.parent as AccountDetailsActivity).updateInvoiceAccount(
                            item.listId,
                            (spinner.selectedItem as SimpleAccount).id
                        )
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        Log.i("TAG", "onBindViewHolder: CANCEL")
                    }
                alert.show()
//                val indent = Intent(holder.parent, InvoiceDetailsActivity::class.java).apply {
//                    this.putExtra("invoiceId", item.listId.toLong())
//                }
//                ContextCompat.startActivity(holder.parent, indent, null)
            }
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class AccountExpensesViewHolder(val binding: InvoiceAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
