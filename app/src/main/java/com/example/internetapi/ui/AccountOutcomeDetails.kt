package com.example.internetapi.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountOutcomeDetailsBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.ui.adapters.AccountExpensesAdapter
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.example.internetapi.ui.viewModel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountOutcomeDetails : AppCompatActivity() {

    private val FAILED_TO_LOAD_ACCOUNT_INVOICES = "Failed to load account invoices"

    private val accountViewModel: AccountViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()
    private lateinit var binding: ActivityAccountOutcomeDetailsBinding
    private lateinit var adapter: AccountExpensesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AccountExpensesAdapter()
        binding.rvInvoices.layoutManager = LinearLayoutManager(this)
        binding.rvInvoices.adapter = adapter

        intent.extras?.let { extra ->
            binding.name.text = extra.getString("name", "")
            binding.outcomeSum.text = extra.getString("outcome", "0.0")
            extra.getInt("accountId").let { accountId ->
                accountViewModel.accountInvoices(accountId).observe(this, {
                    when (it.status) {
                        Status.SUCCESS -> loadOnSuccess(it, accountId)
                        Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_ACCOUNT_INVOICES)
                        Status.LOADING -> {}
                    }
                })
            }
        }
    }

    fun updateInvoiceAccount(invoiceId: Long, oldAccount: Int, accountId: Int) {
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

    private fun loadOnSuccess(it: Resource<List<AccountInvoice>>, accountId: Int) {
        binding.progress.visibility = View.GONE
        binding.rvInvoices.visibility = View.VISIBLE
        it.data?.let {
            adapter.submitList(it, accountId)
        }
    }
}