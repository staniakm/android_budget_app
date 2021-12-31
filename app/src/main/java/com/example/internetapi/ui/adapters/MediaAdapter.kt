package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.MediaAdapterBinding
import com.example.internetapi.models.MediaType

class MediaAdapter : RecyclerView.Adapter<MediaViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<MediaType>() {
        override fun areItemsTheSame(oldItem: MediaType, newItem: MediaType): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaType, newItem: MediaType): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<MediaType>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {

        val binding =
            MediaAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MediaViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            mediaName.text = item.name
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun addNewMediaType(res: MediaType) {
        differ.currentList.toMutableList().apply {
            add(res)
        }.let {
            differ.submitList(it)
        }

    }
}

class MediaViewHolder(val binding: MediaAdapterBinding, val parent: Context) :
    RecyclerView.ViewHolder(binding.root) {}
