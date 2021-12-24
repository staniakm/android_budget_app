package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountIncomeDetailsBinding
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.AccountIncomesAdapter
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountIncomeDetails : AppCompatActivity() {
    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountIncomeDetailsBinding
    private lateinit var incomeAdapter: AccountIncomesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountIncomeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        incomeAdapter = AccountIncomesAdapter()
        binding.rvIncomes.layoutManager = LinearLayoutManager(this)
        binding.rvIncomes.adapter = incomeAdapter


        intent.extras?.let { extra ->
            binding.incomeSum.text = extra.getString("income", "0.0")
            binding.name.text = extra.getString("name", "")
            extra.getInt("accountId").let { accountId ->
                accountViewModel.getAccountIncome(accountId).observe(this, {
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
            }
        }
    }

    private fun loadOnSuccessIncome(it: Resource<List<AccountIncome>>) {
        binding.progress.visibility = View.GONE
        binding.rvIncomes.visibility = View.VISIBLE
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
}