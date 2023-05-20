package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.CategoryAdapterBinding
import com.example.internetapi.models.Category

class CategoryAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<CategoryViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Category>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        val binding =
            CategoryAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CategoryViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            categoryName.text = item.name
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getItem(position: Int): Category = differ.currentList[position]

    fun addNewCategory(res: Category) {
        differ.currentList.toMutableList().apply {
            add(res)
        }.let {
            differ.submitList(it)
        }

    }
}

class CategoryViewHolder(
    val binding: CategoryAdapterBinding,
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
            layout.id -> adapterPosition.let {
                if (it != RecyclerView.NO_POSITION) {
                    listener.onClick(it, "layout")
                }
            }
        }
    }
}
