package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.internetapi.databinding.ActivityUpdateBudgetBinding
import com.example.internetapi.models.*
import com.example.internetapi.ui.viewModel.BudgetViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class UpdateBudgetActivity : AppCompatActivity() {
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
                    budgetViewModel.updateBudget(
                        UpdateBudgetRequest(
                            budget.budgetId,
                            BigDecimal(binding.planned.text.toString())
                        )
                    ).observe(this, {
                        when (it.status) {
                            Status.SUCCESS -> updateAdapter(it.data)
                            Status.ERROR -> Snackbar.make(
                                binding.root,
                                "failed update account data",
                                Snackbar.LENGTH_LONG
                            )
                                .show()
                            Status.LOADING -> Log.println(
                                Log.DEBUG,
                                "UpdateBudget..",
                                "Loading....."
                            )
                        }
                    })
                }

            }
        }
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