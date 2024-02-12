package com.example.internetapi.ui.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.R
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.CategoryDetailsAdapterBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.Category
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.models.Status
import com.example.internetapi.ui.CategoryDetailsActivity
import com.example.internetapi.ui.viewModel.CategoryViewModel


class CategoryDetailsAdapter(
    private val viewModel: CategoryViewModel,
    private val categoryDetailsActivity: CategoryDetailsActivity
) : RecyclerView.Adapter<CategoryDetailsViewHolder>() {
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
                            Log.i(
                                TAG,
                                "onBindViewHolder: change category for itemId ${item.assortmentId}"
                            )
                            getCategories(holder, item)
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

    private fun getCategories(holder: CategoryDetailsViewHolder, categoryDetails: CategoryDetails) {
        viewModel.getCategories().observe(categoryDetailsActivity) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(holder, it, categoryDetails)
                Status.ERROR -> errorSnackBar(
                    holder.binding.root,
                    "Failed to load categories"
                )

                Status.LOADING -> Log.println(Log.DEBUG, "CategoryLoading", "Loading.....")
            }
        }
    }

    private fun processSuccess(
        holder: CategoryDetailsViewHolder,
        resource: Resource<List<Category>>,
        categoryDetails: CategoryDetails
    ) {
        resource.data?.let { categories ->
            holder.binding.root.parent?.let {
                (it as ViewGroup).removeView(holder.binding.root)
            }
            val spinner = Spinner(holder.context)
            spinner.adapter = ArrayAdapter(
                holder.context,
                android.R.layout.simple_spinner_dropdown_item,
                categories.sortedBy { it.name }
            )
            spinner.setSelection(categories.indexOfFirst { it.id == 1 })

            val alert: AlertDialog.Builder = AlertDialog.Builder(holder.context)
            alert.setTitle("Change category")
                .setMessage(categoryDetails.name)
                .setView(spinner)
                .setPositiveButton("OK") { _, _ ->
                    Log.i(
                        TAG,
                        "processSuccess: OK clicked: ${(spinner.selectedItem as Category).id}"
                    )
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Log.i(TAG, "processSuccess: cancel clicked")
                }
            alert.show()
        } ?: errorSnackBar(
            holder.binding.root,
            "Failed to load categories"
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}

class CategoryDetailsViewHolder(val binding: CategoryDetailsAdapterBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root)