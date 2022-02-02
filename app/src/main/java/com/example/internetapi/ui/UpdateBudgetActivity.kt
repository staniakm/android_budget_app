package com.example.internetapi.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.R
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.databinding.ActivityUpdateBudgetBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.*
import com.example.internetapi.ui.adapters.BudgetItemAdapter
import com.example.internetapi.ui.viewModel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class UpdateBudgetActivity : AppCompatActivity() {
    private val TAG: String = "UpdateBudgetActivity"
    private val FAILED_TO_UPDATE_BUDGET = "Failed to update budget data"

    private val budgetViewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityUpdateBudgetBinding
    private lateinit var adapter: BudgetItemAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBudgetBinding.inflate(layoutInflater)
        adapter = BudgetItemAdapter()
        binding.rvBudgetItems.layoutManager = LinearLayoutManager(this)
        binding.rvBudgetItems.adapter = adapter
        setContentView(binding.root)
        intent.extras?.let { extra ->
            extra.getSerializable("budget")?.let { budgetObjects ->
                val budget = budgetObjects as MonthBudget
                with(budget) {
                    binding.category.text = category
                    binding.spendValue.text = df.format(spent)
                    binding.percentageValue.text = "$percentage %"
                    binding.planedValue.text = df.format(planned)
                }
                binding.edit.setOnClickListener {
                    createUpdateBudgetDialog(budget)
                }
                loadBudgetItems(budget.budgetId)
            }
        }
    }

    private fun loadBudgetItems(budgetId: Int) {
        budgetViewModel.getBudgetItems(budgetId).observe(this) {
            when (it.status) {
                Status.SUCCESS -> budgetItemsLoaded(it.data)
                Status.ERROR -> errorSnackBar(binding.root, "Unable to load budget items")
                Status.LOADING -> Log.i(TAG, "loadBudgetItems: LOADING")
            }
        }
    }

    private fun budgetItemsLoaded(items: List<InvoiceDetails>?) {
        items?.let {
            adapter.submitList(it)
        }
    }

    private fun updateBudget(budgetId: Int, planned: BigDecimal) {
        budgetViewModel.updateBudget(
            UpdateBudgetRequest(
                budgetId, planned
            )
        ).observe(this) {
            when (it.status) {
                Status.SUCCESS -> updateAdapter(it.data)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_UPDATE_BUDGET)
                Status.LOADING -> {}
            }
        }
    }


    private fun createUpdateBudgetDialog(budget: MonthBudget) {
        val edit = EditText(this)
        edit.setText(df.format(budget.planned))
        edit.inputType = InputType.TYPE_CLASS_NUMBER

        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Update planed value for budget")
            .setMessage("Spend: ${budget.spent}")
            .setView(edit)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val income = edit.text.toString()
                when (val value = income.toBigDecimalOrNull()) {
                    null -> errorSnackBar(
                        binding.root,
                        "Provided value: $income - is not parsable to number"
                    )
                    else -> updateBudget(budgetId = budget.budgetId, value)
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alert.show()
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