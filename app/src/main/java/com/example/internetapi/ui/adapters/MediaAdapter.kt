package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.MediaAdapterBinding
import com.example.internetapi.models.Account
import com.example.internetapi.models.MediaType

class MediaAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<MediaViewHolder>() {

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

        return MediaViewHolder(binding, parent.context, listener)
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

    fun getItem(position: Int): MediaType = differ.currentList[position]

    fun addNewMediaType(res: MediaType) {
        differ.currentList.toMutableList().apply {
            add(res)
        }.let {
            differ.submitList(it)
        }

    }
}

class MediaViewHolder(
    val binding: MediaAdapterBinding,
    val parent: Context,
    private val listener: OnItemClickedListener
) :
    RecyclerView.ViewHolder(binding.root), View.OnClickListener {
    private val layout = binding.layout

    init {
        layout.setOnClickListener(this)
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
}
