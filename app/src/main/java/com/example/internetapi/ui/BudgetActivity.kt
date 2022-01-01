package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.config.ActivityResultCodes
import com.example.internetapi.config.DateFormatter.yyyymm
import com.example.internetapi.databinding.ActivityBudgetBinding
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.ui.adapters.MonthBudgetAdapter
import com.example.internetapi.ui.viewModel.BudgetViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.time.LocalDate

@AndroidEntryPoint
class BudgetActivity : AppCompatActivity() {
    private var df: DecimalFormat = DecimalFormat("##0.00")

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityBudgetBinding
    private lateinit var adapter: MonthBudgetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindAdapter()

        binding.monthManipulator.previous.setOnClickListener {
            MonthSelector.previous()
            loadData()
        }

        binding.monthManipulator.next.setOnClickListener {
            if (MonthSelector.month < 0) {
                MonthSelector.next()
                loadData()
            }
        }

        binding.monthManipulator.date.setOnClickListener {
            if (MonthSelector.month != 0) {
                MonthSelector.current()
                loadData()
            }
        }

        binding.recalculate.setOnClickListener {
            recalculateBudgets()
            loadData()
        }

        loadData()

    }

    private fun recalculateBudgets() {
        viewModel.recalculateBudgets().observe(this, {
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

    private fun loadData() {
        val date = LocalDate.now().withDayOfMonth(1)
        binding.monthManipulator.date.text = date.plusMonths(MonthSelector.month.toLong())
            .format(yyyymm)
        binding.monthManipulator.previous.text = date.plusMonths(MonthSelector.month.toLong() - 1)
            .format(yyyymm)
        binding.monthManipulator.next.text = date.plusMonths(MonthSelector.month.toLong() + 1)
            .format(yyyymm)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityResultCodes.UPDATE_BUDGET) {
            when (resultCode) {
                RESULT_OK -> data?.getSerializableExtra("result")?.let {
                    val budget = it as UpdateBudgetResponse
                    adapter.updateBudget(budget)
                    binding.totalPlaned.text = "Zaplanowane: ${df.format(budget.monthPlanned)}"
                }
                RESULT_CANCELED -> Log.i("TAG", "onActivityResult: NO RESULT RETURNED")
            }
        }
    }

    private fun bindAdapter() {
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = MonthBudgetAdapter()
        binding.rvBudgets.layoutManager = LinearLayoutManager(this)
        binding.rvBudgets.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
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