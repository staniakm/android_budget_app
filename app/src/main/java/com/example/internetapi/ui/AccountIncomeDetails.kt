package com.example.internetapi.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountIncomeDetailsBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.AccountIncomesAdapter
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountIncomeDetails : AppCompatActivity() {

    private val FAILED_TO_LOAD_ACCOUNT_INCOME = "Failed to load account income"

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
                        Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_ACCOUNT_INCOME)
                        Status.LOADING -> {}
                    }
                })
            }
        }
    }

    private fun loadOnSuccessIncome(it: Resource<List<AccountIncome>>) {
        binding.progress.visibility = View.GONE
        binding.rvIncomes.visibility = View.VISIBLE
        it.data.let { res ->
            res?.let {
                incomeAdapter.submitList(it)
            }
        }
    }
}