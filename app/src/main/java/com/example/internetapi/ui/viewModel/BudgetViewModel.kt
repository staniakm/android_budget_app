package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.Budget
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.UpdateBudgetRequest
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(private val repository: BudgetRepository) :
    ViewModelDataFunction() {

    fun getBudgets(): LiveData<Resource<Budget>> {
        return executeLiveDataSingle { repository.getBudgets() }
    }

    fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): LiveData<Resource<UpdateBudgetResponse>> {
        return executeLiveDataSingle { repository.updateBudget(updateBudgetRequest) }
    }

    fun recalculateBudgets(): LiveData<Resource<Budget>> {
        return executeLiveDataSingle { repository.recalculateBudgets() }
    }

    fun getBudgetItems(budgetId: Int): LiveData<Resource<List<InvoiceDetails>>> {
        return executeLiveDataList { repository.getBudgetItems(budgetId) }
    }
}