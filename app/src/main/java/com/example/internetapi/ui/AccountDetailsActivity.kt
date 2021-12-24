package com.example.internetapi.ui

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.databinding.IncomeViewBinding
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.IncomeType
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
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
        accountViewModel.getIncomeTypes().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccessIncome(it.data, name, accountId)
                Status.ERROR -> Snackbar.make(
                    binding.rootView,
                    "failed fetched data",
                    Snackbar.LENGTH_LONG
                ).show()
                Status.LOADING -> Log.println(Log.DEBUG, "AccountDetails", "Loading.....")
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
                R.layout.simple_spinner_dropdown_item,
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
                            toDate(incomeBinding.date),
                            incomeBinding.description.selectedItem as String
                        )
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    Log.i("TAG", "onBindViewHolder: CANCEL")
                }
            alert.show()
        }
    }

    private fun toDate(date: DatePicker): LocalDate {
        return LocalDate.of(date.year, date.month, date.dayOfMonth)
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