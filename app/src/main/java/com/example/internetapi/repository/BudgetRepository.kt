package com.example.internetapi.repository

import com.example.internetapi.api.BudgetApiHelper
import com.example.internetapi.models.UpdateBudgetRequest
import javax.inject.Inject

class BudgetRepository @Inject constructor(private val apiHelper: BudgetApiHelper) {
    suspend fun getBudgets() = apiHelper.getBudgets()
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest) =
        apiHelper.updateBudget(updateBudgetRequest)

    suspend fun recalculateBudgets() = apiHelper.recalculateBudgets()
}