package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.BudgetItemAdapterBinding
import com.example.internetapi.models.InvoiceDetails

class BudgetItemAdapter : RecyclerView.Adapter<BudgetItemsViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<InvoiceDetails>() {
        override fun areItemsTheSame(
            oldItem: InvoiceDetails,
            newItem: InvoiceDetails
        ): Boolean {
            return oldItem.invoiceItemId == newItem.invoiceItemId
        }

        override fun areContentsTheSame(
            oldItem: InvoiceDetails,
            newItem: InvoiceDetails
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<InvoiceDetails>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetItemsViewHolder {

        val binding =
            BudgetItemAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return BudgetItemsViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: BudgetItemsViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            name.text = item.productName
            price.text = MoneyFormatter.df.format(item.price)
            quantity.text = MoneyFormatter.df.format(item.quantity)
            priceTotal.text = MoneyFormatter.df.format(item.totalPrice)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getItem(position: Int): InvoiceDetails {
        return differ.currentList[position]
    }
}

class BudgetItemsViewHolder(
    val binding: BudgetItemAdapterBinding,
    val parent: Context
) :
    RecyclerView.ViewHolder(binding.root)
