package com.example.internetapi.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat
import com.example.internetapi.R
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.DateFormatter.yyyymm
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.databinding.IncomeViewBinding
import com.example.internetapi.databinding.TransferViewBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.toLocalDate
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class AccountDetailsActivity : AppCompatActivity() {

    private val FAILED_TO_GET_INCOME_TYPE = "Failed to load income type"

    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var incomeBinding: IncomeViewBinding
    private lateinit var transferBinding: TransferViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        incomeBinding = IncomeViewBinding.inflate(layoutInflater)
        transferBinding = TransferViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            val name = extra.getString("name", "")
            val income = extra.getString("income", "0.0").toString()
            val outcome = extra.getString("outcome", "0.0").toString()
            val accountId = extra.getInt("accountId")
            binding.name.text =
                "$name - ${LocalDate.now().plusMonths(MonthSelector.month.toLong()).format(yyyymm)}"
            binding.incomeSum.text = income
            binding.outcomeSum.text = outcome

            binding.incomeLay.setOnClickListener {
                Intent(this, AccountIncomeDetails::class.java).apply {
                    this.putExtra("name", name)
                    this.putExtra("accountId", extra.getInt("accountId"))
                    this.putExtra("income", income)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }
            binding.outcomeLay.setOnClickListener {
                Intent(this, AccountOutcomeDetails::class.java).apply {
                    this.putExtra("name", name)
                    this.putExtra("accountId", accountId)
                    this.putExtra("outcome", outcome)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_income_menu) {
            intent.extras?.let { extra ->
                val name = extra.getString("name", "")
                val accountId = extra.getInt("accountId")
                createIncomeAddDialog(name, accountId)
            }
        } else if (item.itemId == R.id.transfer_money_menu) {
            intent.extras?.let { extra ->
                val name = extra.getString("name", "")
                val accountId = extra.getInt("accountId")
                createMoveMoneyDialog(name, accountId)
            }
        } else if (item.itemId == R.id.add_invoice_menu) {
            intent.extras?.let { extra ->
                val name = extra.getString("name", "")
                val accountId = extra.getInt("accountId")
                Intent(this, AccountOutcomeRegisterActivity::class.java).apply {
                    this.putExtra("accountId", accountId)
                    this.putExtra("accountName", name)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createMoveMoneyDialog(name: String, accountId: Int) {
        transferBinding.root.parent?.let {
            (it as ViewGroup).removeView(transferBinding.root)
        }
        transferBinding.targetAccount.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            AccountHolder.accounts
        )
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Transfer money")
            .setMessage(name)
            .setView(transferBinding.root)
            .setPositiveButton("OK") { _, _ ->
                val income = transferBinding.value.text.toString()
                val targetAccount = (transferBinding.targetAccount.selectedItem as SimpleAccount).id
                when (val value = income.toBigDecimalOrNull()) {
                    null -> errorSnackBar(
                        binding.root,
                        "Provided value: $income - is not parsable to number"
                    )
                    else -> this.transferMoney(
                        accountId,
                        value,
                        targetAccount
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
        alert.show()


    }

    private fun transferMoney(accountId: Int, value: BigDecimal, targetAccount: Int) {
        if (value > BigDecimal.ZERO && accountId != targetAccount) {
            TransferMoneyRequest(
                accountId,
                value,
                targetAccount
            ).let {
                accountViewModel.transferMoney(it)
            }
        }
    }


    private fun createIncomeAddDialog(name: String, accountId: Int) {
        accountViewModel.getIncomeTypes().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccessIncome(it.data, name, accountId)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_GET_INCOME_TYPE)
                Status.LOADING -> {}
            }
        })
        incomeBinding.root.parent?.let {
            (it as ViewGroup).removeView(incomeBinding.root)
        }
    }

    private fun loadOnSuccessIncome(
        descriptions: List<IncomeType>?,
        name: String,
        accountId: Int
    ) {
        if (descriptions != null) {
            incomeBinding.description.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                descriptions.map { it.name }.toMutableList()
            )
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle("Add account income")
                .setMessage(name)
                .setView(incomeBinding.root)
                .setPositiveButton("OK") { _, i ->
                    val income = incomeBinding.value.text.toString()
                    when (val v = income.toBigDecimalOrNull()) {
                        null -> Log.w(
                            "AccountDetails",
                            "Income value is not parsable to BigDecimal"
                        )
                        else -> this.addIncome(
                            accountId,
                            v,
                            incomeBinding.date.toLocalDate(),
                            incomeBinding.description.selectedItem as String
                        )
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }
            alert.show()
        }
    }

    private fun addIncome(accountId: Int, value: BigDecimal, date: LocalDate, description: String) {
        if (value > BigDecimal.ZERO) {
            AccountIncomeRequest(
                accountId,
                value,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                description
            ).let {
                accountViewModel.addIncome(it)
            }
        }
    }
}