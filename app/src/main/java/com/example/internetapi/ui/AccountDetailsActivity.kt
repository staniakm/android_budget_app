package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.AccountExpensesAdapter
import com.example.internetapi.ui.adapters.AccountIncomesAdapter
import com.example.internetapi.ui.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountDetailsActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var adapter: AccountExpensesAdapter
    private lateinit var incomeAdapter: AccountIncomesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AccountExpensesAdapter()
        incomeAdapter = AccountIncomesAdapter()
        binding.rvInvoices.layoutManager = LinearLayoutManager(this)
        binding.rvInvoices.adapter = adapter
        binding.rvIncomes.layoutManager = LinearLayoutManager(this)
        binding.rvIncomes.adapter = incomeAdapter


        intent.extras?.let { extra ->
            extra.getLong("accountId")?.let { accountId ->
                mainViewModel.getAccountIncome(accountId).observe(this, {
                    when (it.status) {
                        Status.SUCCESS -> loadOnSuccessIncome(it)
                        Status.ERROR -> Snackbar.make(
                            binding.rootView,
                            "failed fetched data",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                        Status.LOADING -> Log.println(Log.DEBUG, "AccountDetails", "Loading.....")
                    }
                })
                mainViewModel.accountInvoices(accountId).observe(this, {
                    when (it.status) {
                        Status.SUCCESS -> loadOnSuccess(it)
                        Status.ERROR -> Snackbar.make(
                            binding.rootView,
                            "failed fetched data",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                        Status.LOADING -> Log.println(Log.DEBUG, "AccountDetails", "Loading.....")
                    }
                })

            }
        }
    }

    private fun loadOnSuccessIncome(it: Resource<List<AccountIncome>>) {
        binding.progress.visibility = View.GONE
        binding.rvInvoices.visibility = View.VISIBLE
        it.data.let { res ->
            if (res != null) {
                res.let { list ->
                    incomeAdapter.submitList(list)
                }
            } else {
                Snackbar.make(binding.root, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadOnSuccess(it: Resource<List<AccountInvoice>>) {
        binding.progress.visibility = View.GONE
        binding.rvInvoices.visibility = View.VISIBLE
        it.data.let { res ->
            if (res != null) {
                res.let { list ->
                    adapter.submitList(list)
                }
            } else {
                Snackbar.make(binding.root, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}