package com.example.internetapi.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.AmountFormatter
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.InvoiceDetailsAdapterBinding
import com.example.internetapi.models.InvoiceItem
import java.math.BigDecimal

class InvoiceItemsAdapter() : RecyclerView.Adapter<InvoiceItemViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<InvoiceItem>() {
        override fun areItemsTheSame(oldItem: InvoiceItem, newItem: InvoiceItem): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: InvoiceItem, newItem: InvoiceItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<InvoiceItem>) = differ.submitList(list)

    fun addItem(item: InvoiceItem): BigDecimal {
        return differ.currentList.toMutableList().let {
            val sum = it.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalPrice()) }
            it.add(item)
            submitList(it)
            sum.plus(item.totalPrice())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceItemViewHolder {

        val binding =
            InvoiceDetailsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return InvoiceItemViewHolder(binding, parent.context)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: InvoiceItemViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            productName.text = item.shopItem.name
            quantity.text = "Ilość:\n${AmountFormatter.df.format(item.amount)}"
            price.text = "Cena:\n${MoneyFormatter.df.format(item.price)}"
            discount.text = "Rabat:\n${MoneyFormatter.df.format(item.discount)}"
            totalPrice.text = "Suma:\n${MoneyFormatter.df.format(item.totalPrice())}"
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun clear() {
        submitList(listOf())
    }

    fun getItems(): MutableList<InvoiceItem> {
        return differ.currentList
    }

    fun removeAt(absoluteAdapterPosition: Int): BigDecimal {
        val item = differ.currentList[absoluteAdapterPosition]
        val sum = differ.currentList.fold(BigDecimal.ZERO) { acc, i -> acc.add(i.totalPrice()) }
            .minus(item.totalPrice())
        differ.currentList.filterIndexed { index, _ -> index != absoluteAdapterPosition }
            .let {
                differ.submitList(it)
            }
        return sum
    }
}

class InvoiceItemViewHolder(val binding: InvoiceDetailsAdapterBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root) {
}
