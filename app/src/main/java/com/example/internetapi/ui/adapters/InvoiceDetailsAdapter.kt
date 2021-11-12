package com.example.internetapi.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.InvoiceAdapterBinding
import com.example.internetapi.databinding.InvoiceDetailsAdapterBinding
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.InvoiceDetails
import java.math.RoundingMode
import java.text.DecimalFormat


class InvoiceDetailsAdapter : RecyclerView.Adapter<InvoiceDetailsViewHolder>() {

    var df: DecimalFormat = DecimalFormat("##0.00")

    private val diffCallback = object : DiffUtil.ItemCallback<InvoiceDetails>() {
        override fun areItemsTheSame(oldItem: InvoiceDetails, newItem: InvoiceDetails): Boolean {
            return oldItem.invoiceItemId == newItem.invoiceItemId
        }

        override fun areContentsTheSame(oldItem: InvoiceDetails, newItem: InvoiceDetails): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<InvoiceDetails>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceDetailsViewHolder {

        val binding =
            InvoiceDetailsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return InvoiceDetailsViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: InvoiceDetailsViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            productName.text = item.productName
            quantity.text = "Ilość:\n${df.format(item.quantity)}"
            price.text = "Cena:\n${df.format(item.price)}"
            discount.text = "Rabat:\n${df.format(item.discount)}"
            totalPrice.text = "Suma: ${df.format(item.totalPrice)}"
            }
        }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

class InvoiceDetailsViewHolder(val binding: InvoiceDetailsAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
