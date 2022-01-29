package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.ActivityUpdateBudgetBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateBudgetRequest
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.ui.viewModel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class UpdateBudgetActivity : AppCompatActivity() {
    private val TAG: String = "UpdateBudgetActivity"
    private val FAILED_TO_UPDATE_BUDGET = "Failed to update budget data"

    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityUpdateBudgetBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.extras?.let { extra ->
            extra.getSerializable("budget")?.let { budgetObjects ->
                val budget = budgetObjects as MonthBudget
                loadBudgetItems(budget.budgetId)
                with(budget) {
                    binding.category.text = category
                    binding.spendValue.text = df.format(spent)
                    binding.percentageValue.text = "$percentage %"
                    binding.planned.setText(planned.toString())
                }
                binding.updateBudget.setOnClickListener {
                    updateBudget(budget)
                }
            }
        }
    }

    private fun loadBudgetItems(budgetId: Int) {
        budgetViewModel.getBudgetItems(budgetId).observe(this){
            when(it.status){
                Status.SUCCESS -> Log.i(TAG, "loadBudgetItems: SUCCESS")
                Status.ERROR -> Log.i(TAG, "loadBudgetItems: FAILURE")
                Status.LOADING -> Log.i(TAG, "loadBudgetItems: LOADING")
            }
        }

    }

    private fun updateBudget(budget: MonthBudget) {
        budgetViewModel.updateBudget(
            UpdateBudgetRequest(
                budget.budgetId,
                BigDecimal(binding.planned.text.toString())
            )
        ).observe(this) {
            when (it.status) {
                Status.SUCCESS -> updateAdapter(it.data)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_UPDATE_BUDGET)
                Status.LOADING -> {}
            }
        }
    }

    private fun updateAdapter(accId: UpdateBudgetResponse?) {
        accId?.let {
            Intent().apply {
                putExtra("result", it)
            }.let {
                setResult(RESULT_OK, it)
            }
        } ?: setResult(RESULT_CANCELED)

        finish()
    }
}