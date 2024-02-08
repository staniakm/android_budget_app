package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateListOf
import com.example.internetapi.api.Resource
import com.example.internetapi.config.DateFormatter.yyyymm
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.ActivityBudgetBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.getResultFromActiviy
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.ui.adapters.BudgetList
import com.example.internetapi.ui.viewModel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class BudgetActivity : AppCompatActivity() {
    private val FAILED_TO_RECALCULATE_BUDGETS: String = "Failed to recalculate budgets"
    private val FAILED_TO_LOAD_BUDGETS: String = "Failed to load budgets data"

    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityBudgetBinding
    private val budgets = mutableStateListOf<MonthBudget>()

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
        viewModel.recalculateBudgets().observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_RECALCULATE_BUDGETS)
                Status.LOADING -> {}
            }
        }
    }

    private fun loadData() {
        val date = LocalDate.now().withDayOfMonth(1)
        binding.monthManipulator.date.text = date.plusMonths(MonthSelector.month.toLong())
            .format(yyyymm)
        binding.monthManipulator.previous.text = date.plusMonths(MonthSelector.month.toLong() - 1)
            .format(yyyymm)
        binding.monthManipulator.next.text = date.plusMonths(MonthSelector.month.toLong() + 1)
            .format(yyyymm)
        viewModel.getBudgets().observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_BUDGETS)
                Status.LOADING -> {}
            }
        }
    }

    private fun bindAdapter() {
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.composeView.setContent {
            MaterialTheme {
                BudgetList(
                    budgets = budgets,
                    { monthBudget -> onClick(monthBudget) })
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun processSuccess(it: Resource<Budget>) {
        it.data?.let { data ->
            binding.totalEarnedValue.text = df.format(data.totalEarned)
            binding.totalPlanedValue.text = df.format(data.totalPlanned)
            binding.totalSpendValue.text = df.format(data.totalSpend)
            budgets.clear()
            budgets.addAll(data.budgets)

        }
    }

    private var resultLauncher = getResultFromActiviy(this) { result ->
        result.data?.getSerializableExtra("result")?.let {
            val budget = it as UpdateBudgetResponse
            updateBudgetsList(budget)
            binding.totalPlanedValue.text = df.format(budget.monthPlanned)
        }
    }

    private fun onClick(item: MonthBudget) {
        Intent(this, UpdateBudgetActivity::class.java).apply {
            this.putExtra("budget", item)
        }.let {
            resultLauncher.launch(it)
        }
    }

    private fun updateBudgetsList(budget: UpdateBudgetResponse) {
        budgets.replaceAll {
            if (it.budgetId == budget.budgetId) it.copy(
                planned = budget.planned,
                percentage = budget.percentage
            ) else it
        }
    }
}