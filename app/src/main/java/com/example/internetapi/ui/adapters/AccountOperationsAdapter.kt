package com.example.internetapi.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.AccountOperationAdapterBinding
import com.example.internetapi.models.AccountOperation
import com.example.internetapi.ui.theme.InternetApiTheme

class AccountOperationAdapter(private val listener: OnItemClickedListener) :
    RecyclerView.Adapter<AccountOperationsViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<AccountOperation>() {
        override fun areItemsTheSame(
            oldItem: AccountOperation,
            newItem: AccountOperation
        ): Boolean {
            return oldItem.id == newItem.id && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(
            oldItem: AccountOperation,
            newItem: AccountOperation
        ): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<AccountOperation>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountOperationsViewHolder {

        val binding =
            AccountOperationAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return AccountOperationsViewHolder(binding, parent.context, listener)
    }

    override fun onBindViewHolder(holder: AccountOperationsViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.apply {
            this.composeView.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    InternetApiTheme {
                        AccountOperationCard(item)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun getItem(position: Int): AccountOperation {
        return differ.currentList[position]
    }
}

class AccountOperationsViewHolder(
    val binding: AccountOperationAdapterBinding,
    val parent: Context,
    val listener: OnItemClickedListener,
) :
    RecyclerView.ViewHolder(binding.root)
