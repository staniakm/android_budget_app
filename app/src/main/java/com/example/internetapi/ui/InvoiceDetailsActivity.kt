package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.R
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityInvoiceDetailsBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.InvoiceDetailsAdapter
import com.example.internetapi.ui.viewModel.InvoiceViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InvoiceDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_LOAD_INVOICE_DETAILS: String = "Failed to load invoice details"
    private val invoiceViewModel: InvoiceViewModel by viewModels()
    private lateinit var binding: ActivityInvoiceDetailsBinding
    private lateinit var adapter: InvoiceDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceDetailsBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_invoice_details)
        setContentView(binding.root)
        adapter = InvoiceDetailsAdapter()
        binding.rvInvoices.layoutManager = LinearLayoutManager(this)
        binding.rvInvoices.adapter = adapter

        intent.extras?.let { extra ->
            val invoiceId = extra.getLong("invoiceId")
            invoiceViewModel.invoiceDetails(invoiceId).observe(this) {
                when (it.status) {
                    Status.SUCCESS -> loadOnSuccess(it)
                    Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_INVOICE_DETAILS)
                    Status.LOADING -> {}
                }
            }
        }
    }

    private fun loadOnSuccess(it: Resource<List<InvoiceDetails>>) {
        binding.progress.visibility = View.GONE
        binding.rvInvoices.visibility = View.VISIBLE
        it.data?.let { list -> adapter.submitList(list) }
    }
}