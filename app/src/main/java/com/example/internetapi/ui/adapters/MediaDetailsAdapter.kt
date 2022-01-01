package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.MediaDetailsAdapterBinding
import com.example.internetapi.models.MediaUsage
import java.math.BigDecimal

class MediaDetailsAdapter : RecyclerView.Adapter<MediaDetailsViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<MediaUsage>() {
        override fun areItemsTheSame(oldItem: MediaUsage, newItem: MediaUsage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaUsage, newItem: MediaUsage): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<MediaUsage>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaDetailsViewHolder {

        val binding =
            MediaDetailsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MediaDetailsViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: MediaDetailsViewHolder, position: Int) {
        val item = differ.currentList[position]
        val previousMonthItem =
            differ.currentList.getOrElse(position + 1) { MediaUsage(-1, 0, 0, BigDecimal.ZERO) }
        holder.binding.apply {
            date.text = "${item.month}-${item.year}"
            lastValue.text = item.meterRead.toString()
            previous.text = previousMonthItem.meterRead.toString()
            change.text = (item.meterRead.minus(previousMonthItem.meterRead)).toString()
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun removeAt(adapterPosition: Int): MediaUsage? {
        val item = differ.currentList[adapterPosition]
        differ.currentList.filterIndexed { index, _ -> index != adapterPosition }
            .let {
                differ.submitList(it)
            }
        return item
    }
}

class MediaDetailsViewHolder(val binding: MediaDetailsAdapterBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root)