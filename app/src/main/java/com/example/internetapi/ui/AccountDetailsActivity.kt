package com.example.internetapi.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.databinding.IncomeViewBinding
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AccountDetailsActivity : AppCompatActivity() {
    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountDetailsBinding
    private lateinit var incomeBinding: IncomeViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        incomeBinding = IncomeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            val name = extra.getString("name", "")
            val income = extra.getString("income", "0.0").toString()
            val outcome = extra.getString("outcome", "0.0").toString()
            val accountId = extra.getInt("accountId")
            binding.name.text = name
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
                    this.putExtra("accountId", extra.getInt("accountId"))
                    this.putExtra("outcome", outcome)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }

            binding.addIncome.setOnClickListener {
                createIncomeAddDialog(name, accountId)
            }
        }
    }


    private fun createIncomeAddDialog(name: String, accountId: Int) {

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add account income")
            .setMessage(name)
            .setView(incomeBinding.root)
            .setPositiveButton("OK") { _, i ->
                val income = incomeBinding.value.text.toString()
                Log.i(
                    "TAG",
                    "onBindViewHolder: OK ${incomeBinding.value.text}  ${incomeBinding.date.dayOfMonth}-${incomeBinding.date.month}"
                )
                when (val v = income.toBigDecimalOrNull()) {
                    null -> Log.w("AccountDetails", "Income value is not parsable to BigDecimal")
                    else -> this.addIncome(accountId, v, toDate(incomeBinding.date))
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.i("TAG", "onBindViewHolder: CANCEL")
            }
        alert.show()
    }

    private fun toDate(date: DatePicker): LocalDate {
        return LocalDate.of(date.year, date.month, date.dayOfMonth)
    }

    private fun addIncome(accountId: Int, value: BigDecimal, date: LocalDate) {
        if (value > BigDecimal.ZERO) {
            AccountIncomeRequest(
                accountId,
                value,
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ).let {
                accountViewModel.addIncome(it)
            }
        }
    }
}