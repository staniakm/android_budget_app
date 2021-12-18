package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.ActivityAccountBinding
import com.example.internetapi.models.Account
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.ui.adapters.AccountAdapter
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountBinding
    private lateinit var adapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AccountAdapter()
        binding.rvAccounts.layoutManager = LinearLayoutManager(this)
        binding.rvAccounts.adapter = adapter

        accountViewModel.getAccounts().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSucess(it)
                Status.LOADING -> loadOnLoading()
                Status.ERROR -> loadOnFailure()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            when (requestCode) {
                RESULT_OK -> data?.getSerializableExtra("result")?.let {
                    adapter.updateListItem(it as UpdateAccountResponse)
                }
                RESULT_CANCELED -> Log.i("TAG", "onActivityResult: NO RESULT RETURNED")
            }
        }
    }


    private fun loadOnFailure() {
        with(binding) {
            progress.visibility = View.GONE
            rvAccounts.visibility = View.VISIBLE
        }
        Snackbar.make(binding.rootView, "Something went wrong", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun loadOnLoading() {
        with(binding) {
            progress.visibility = View.VISIBLE
            rvAccounts.visibility = View.GONE
        }
    }

    private fun loadOnSucess(it: Resource<List<Account>>) {
        binding.progress.visibility = View.GONE
        binding.rvAccounts.visibility = View.VISIBLE
        it.data.let { res ->
            if (res != null) {
                res.let { list ->
                    adapter.submitList(list)
                    val (totalIncome, totalExpanse) = summary(list)
                    binding.summary.text = "$totalIncome\n$totalExpanse"
                }
            } else {
                Snackbar.make(binding.rootView, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun summary(list: List<Account>): Pair<String, String> {
        val totalIncome =
            "Przychód: " + list.map { i -> i.income }
                .sumByDouble { i -> i.toDouble() }
                .let {
                    MoneyFormatter.df.format(it)
                }
        val totalExpanse =
            "Wydatki: " + list.map { i -> i.expense }
                .sumByDouble { it.toDouble() }
                .let {
                    MoneyFormatter.df.format(it)
                }
        return Pair(totalIncome, totalExpanse)
    }
}