package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import javax.inject.Inject

class BudgetRepository @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun getBudgets() = apiHelper.getBudgets()
}