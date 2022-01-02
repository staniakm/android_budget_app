package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
                with(budget) {
                    binding.category.text = category
                    binding.spend.text = "Wydane: $spent"
                    binding.percentage.text = "Procent: $percentage %"
                    binding.planned.setText(planned.toString())
                }
                binding.updateBudget.setOnClickListener {
                    updateBudget(budget)
                }
            }
        }
    }

    private fun updateBudget(budget: MonthBudget) {
        budgetViewModel.updateBudget(
            UpdateBudgetRequest(
                budget.budgetId,
                BigDecimal(binding.planned.text.toString())
            )
        ).observe(this, {
            when (it.status) {
                Status.SUCCESS -> updateAdapter(it.data)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_UPDATE_BUDGET)
                Status.LOADING -> {}
            }
        })
    }

    private fun updateAdapter(accId: UpdateBudgetResponse?) {
        val returnIntent = Intent()
        accId?.let {
            returnIntent.putExtra("result", it)
            setResult(RESULT_OK, returnIntent)
        } ?: setResult(RESULT_CANCELED)

        finish()
    }
}