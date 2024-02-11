package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import com.example.internetapi.api.Resource
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.DateFormatter
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.databinding.ActivityAccountBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.getResultFromActiviy
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Account
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.ui.adapters.AccountInfoList
import com.example.internetapi.ui.theme.InternetApiTheme
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountBinding
    private val accounts = mutableStateListOf<Account>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.composeView.setContent {
            InternetApiTheme {
                AccountInfoList(accounts = accounts,
                    surfaceClick = { account -> onLayoutClick(account) },
                    editAccountClick = { account: Account -> onEditAccountClick(account) })
            }
        }
        loadData()

        binding.monthManipulator.previous.setOnClickListener {
            MonthSelector.previous()
            loadData()
        }
        binding.monthManipulator.date.setOnClickListener {
            MonthSelector.current()
            loadData()
        }
        binding.monthManipulator.next.setOnClickListener {
            if (MonthSelector.month < 0) {
                MonthSelector.next()
                loadData()
            }
        }
    }

    private fun loadData() {
        accountViewModel.getAccounts().observe(this) {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccess(it)
                Status.LOADING -> loadOnLoading()
                Status.ERROR -> loadOnFailure()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadOnFailure() {
        with(binding) {
            progress.visibility = View.GONE
            rvAccounts.visibility = View.GONE
        }
        errorSnackBar(binding.root, "Something went wrong")
    }

    private fun loadOnLoading() {
        with(binding) {
            progress.visibility = View.VISIBLE
            rvAccounts.visibility = View.GONE
        }
    }

    private fun loadOnSuccess(it: Resource<List<Account>>) {
        binding.progress.visibility = View.GONE
        binding.rvAccounts.visibility = View.VISIBLE
        binding.monthManipulator.date.text =
            LocalDate.now().withDayOfMonth(1).plusMonths(MonthSelector.month.toLong())
                .format(DateFormatter.yyyymm)
        it.data.let { res ->
            res?.let { list ->
                accounts.clear()
                accounts.addAll(list)
                AccountHolder.accounts = list.map { it.toSimpleAccount() }.toMutableList()
            } ?: Snackbar.make(binding.rootView, "Status = false", Snackbar.LENGTH_SHORT).show()
        }
    }

    private var updateActivity = getResultFromActiviy(this) { result ->
        result.data?.getSerializableExtra("result")?.let {
            val account = it as UpdateAccountResponse
            updateAccountList(account)
        }
    }

    private fun onLayoutClick(item: Account) {
        Intent(this, AccountDetailsActivity::class.java).apply {
            this.putExtra("name", item.name)
            this.putExtra("accountId", item.id)
            this.putExtra("income", MoneyFormatter.df.format(item.income))
            this.putExtra("outcome", MoneyFormatter.df.format(item.expense))
        }.let {
            ContextCompat.startActivity(this, it, null)
        }
    }

    private fun onEditAccountClick(item: Account) {
        Intent(this, AccountUpdateActivity::class.java).apply {
            this.putExtra("account", item)
        }.let {
            updateActivity.launch(it)
        }
    }

    private fun updateAccountList(accountResponse: UpdateAccountResponse) {
        accounts.replaceAll {
            if (it.id == accountResponse.id.toInt())
                it.copy(name = accountResponse.name, moneyAmount = accountResponse.amount)
            else it
        }
    }
}