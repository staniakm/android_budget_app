package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.models.AccountInvoice


class AccountExpensesAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<AccountExpensesViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AccountInvoice>() {
        override fun areItemsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.listId == newItem.listId
        }

        override fun areContentsTheSame(oldItem: AccountInvoice, newItem: AccountInvoice): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<AccountInvoice>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountExpensesViewHolder {

        val binding =
            InvoiceAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return AccountExpensesViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: AccountExpensesViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            shopName.text = item.name
            cost.text = df.format(item.price)
            date.text = item.date
        }
    }


    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun removeInvoice(invoiceId: Long) {
        differ.currentList.filter { it.listId != invoiceId }.let {
            submitList(it)
        }
    }

    fun getItem(position: Int): AccountInvoice {
        return differ.currentList[position]
    }
}

class AccountExpensesViewHolder(
    val binding: InvoiceAdapterBinding,
    val parent: Context,
    private val listener: OnItemClickedListener
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {
    private val layout = binding.layout

    init {
        layout.setOnClickListener(this)
        layout.setOnLongClickListener(this)
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

    override fun onLongClick(v: View?): Boolean {
        when (v?.id) {
            layout.id -> bindingAdapterPosition.let {
                if (it != RecyclerView.NO_POSITION) {
                    listener.onClick(it, "layoutLong")
                }
            }
        }
        return true
    }
}

