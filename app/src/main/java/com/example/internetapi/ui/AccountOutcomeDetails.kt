package com.example.internetapi.ui

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.databinding.ActivityAccountOutcomeDetailsBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.successSnackBar
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.SimpleAccount
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.ui.adapters.AccountExpensesAdapter
import com.example.internetapi.ui.adapters.OnItemClickedListener
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.example.internetapi.ui.viewModel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class AccountOutcomeDetails : AppCompatActivity(), OnItemClickedListener {
    private val INVOICE_REMOVED: String = "Selected invoice removed"
    private val FAILED_TO_REMOVE_INVOICE: String = "Faile to revmoce selected invoice"
    private val TAG = "AccountOutcomeDetails"
    private val FAILED_TO_LOAD_ACCOUNT_INVOICES = "Failed to load account invoices"

    private val accountViewModel: AccountViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()
    private lateinit var binding: ActivityAccountOutcomeDetailsBinding
    private lateinit var adapter: AccountExpensesAdapter
    private var accountId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AccountExpensesAdapter(this)
        binding.rvInvoices.layoutManager = LinearLayoutManager(this)
        binding.rvInvoices.adapter = adapter

        intent.extras?.let { extra ->
            binding.name.text = extra.getString("name", "")
            binding.outcomeSum.text = extra.getString("outcome", "0.0")
            extra.getInt("accountId").let { accountId ->
                this.accountId = accountId
                accountViewModel.accountInvoices(accountId).observe(this) {
                    when (it.status) {
                        Status.SUCCESS -> loadOnSuccess(it)
                        Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_ACCOUNT_INVOICES)
                        Status.LOADING -> {}
                    }
                }
            }
        }
    }

    private fun updateInvoiceAccount(invoiceId: Long, oldAccount: Int, accountId: Int) {
        invoiceViewModel.updateInvoiceAccount(
            UpdateInvoiceAccountRequest(
                invoiceId,
                oldAccount,
                accountId
            )
        )
        if (oldAccount != accountId) {
            adapter.removeInvoice(invoiceId)
        }
    }

    private fun loadOnSuccess(it: Resource<List<AccountInvoice>>) {
        binding.progress.visibility = View.GONE
        binding.rvInvoices.visibility = View.VISIBLE
        it.data?.let {
            adapter.submitList(it)
        }
    }

    override fun onClick(position: Int, element: String) {
        val item = adapter.getItem(position)
        when (element) {
            "layout" -> Intent(this, InvoiceDetailsActivity::class.java).apply {
                this.putExtra("invoiceId", item.listId)
            }.let {
                ContextCompat.startActivity(this, it, null)
            }
            "layoutLong" -> createSpinner(item)
            "deleteInvoice" -> removeItemPopup(item)
        }
    }

    private fun removeItemPopup(item: AccountInvoice) {
        Log.i(TAG, "removeItemPopup: $item")
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Invoice removal")
            .setMessage("Do you want to remove selected invoice?")
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                invoiceViewModel.deleteInvoice(item.listId).observe(this) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            successSnackBar(binding.root, INVOICE_REMOVED)
                            adapter.removeInvoice(item.listId)
                        }
                        Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_REMOVE_INVOICE)
                        Status.LOADING -> {}
                    }
                }

            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alert.show()

    }

    private fun createSpinner(
        item: AccountInvoice
    ) {
        val accounts = AccountHolder.accounts
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_dropdown_item,
            accounts
        )
        spinner.setSelection(accounts.indexOfFirst { it.id == accountId })
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Change account for selected invoice")
            .setMessage("${item.listId}")
            .setView(spinner)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                this.updateInvoiceAccount(
                    item.listId,
                    accountId,
                    (spinner.selectedItem as SimpleAccount).id
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alert.show()
    }

}