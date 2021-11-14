package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityBudgetBinding
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.MonthBudgetAdapter
import com.example.internetapi.ui.viewModel.BudgetViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class BudgetActivity : AppCompatActivity() {
    private var df: DecimalFormat = DecimalFormat("##0.00")

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityBudgetBinding
    private lateinit var adapter: MonthBudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = MonthBudgetAdapter()
        binding.rvBudgets.layoutManager = LinearLayoutManager(this)
        binding.rvBudgets.adapter = adapter

        binding.previous.setOnClickListener {
            MonthSelector.previous()
            finish()
            startActivity(intent)
        }

        binding.next.setOnClickListener {
            if (MonthSelector.month < 0) {
                MonthSelector.next()
                finish()
                startActivity(intent)
            }
        }

        binding.date.setOnClickListener {
            if (MonthSelector.month != 0) {
                MonthSelector.current()
                finish()
                startActivity(intent)
            }
        }

        binding.date.text = LocalDate.now().plusMonths(MonthSelector.month.toLong())
            .format(DateTimeFormatter.ofPattern("YYYY-MM"))
        binding.previous.text = LocalDate.now().plusMonths(MonthSelector.month.toLong() - 1)
            .format(DateTimeFormatter.ofPattern("YYYY-MM"))
        binding.next.text = LocalDate.now().plusMonths(MonthSelector.month.toLong() + 1)
            .format(DateTimeFormatter.ofPattern("YYYY-MM"))

        viewModel.getBudgets().observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    "failed fetched data",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                Status.LOADING -> Log.println(Log.DEBUG, "InvoiceDetails", "Loading.....")
            }
        })

    }

    private fun processSuccess(it: Resource<Budget>) {
        it.data.let { res ->
            if (res != null) {
                res.let { data ->
                    binding.totalEarned.text = "Zarobione: ${df.format(data.totalEarned)}"
                    binding.totalPlaned.text = "Zaplanowane: ${df.format(data.totalPlanned)}"
                    binding.totalSpend.text = "Wydane: ${df.format(data.totalSpend)}"
                    adapter.submitList(data.budgets)
                }
            } else {
                Snackbar.make(binding.root, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}