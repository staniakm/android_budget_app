package com.example.internetapi.ui.adapters

import com.example.internetapi.R
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.databinding.CategoryDetailsAdapterBinding
import com.example.internetapi.models.CategoryDetails


class CategoryDetailsAdapter : RecyclerView.Adapter<CategoryDetailsViewHolder>() {
    private val TAG = "CategoryDetailsAdapter"
    private val diffCallback = object : DiffUtil.ItemCallback<CategoryDetails>() {
        override fun areItemsTheSame(oldItem: CategoryDetails, newItem: CategoryDetails): Boolean {
            return oldItem.assortmentId == newItem.assortmentId
        }

        override fun areContentsTheSame(
            oldItem: CategoryDetails,
            newItem: CategoryDetails
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<CategoryDetails>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailsViewHolder {

        val binding =
            CategoryDetailsAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return CategoryDetailsViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: CategoryDetailsViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            assortmentName.text = "${item.name}"
            month.text = "${item.price}"

            textViewOptions.setOnClickListener {
                val popup = PopupMenu(holder.context, holder.binding.textViewOptions)
                popup.inflate(R.menu.menu_category_item)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.change_category -> {
                            Log.i(TAG, "onBindViewHolder: change category for itemId ${item.assortmentId}")
                            true
                        }

                        R.id.show_month_expenses -> {
                            Log.i(TAG, "onBindViewHolder: show_month_expenses")
                            true
                        }

                        else -> {
                            false
                        }
                    }

                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}

class CategoryDetailsViewHolder(val binding: CategoryDetailsAdapterBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root)