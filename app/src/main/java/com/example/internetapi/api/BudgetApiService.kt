package com.example.internetapi.api

import com.example.internetapi.constant.Constant.BUDGET
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
interface BudgetApiService {


    @GET(BUDGET)
    suspend fun getBudget(@Query("month") month: Int): Response<Budget>

    @PUT(BUDGET)
    suspend fun updateBudget(@Body updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>

    @PUT("$BUDGET/recalculate")
    suspend fun recalculateBudgets(@Query("month") month: Int): Response<Budget>

    @GET("$BUDGET/{budgetId}")
    suspend fun getBudgetItems(@Path("budgetId") budgetId: Int): Response<List<InvoiceDetails>>
}

class BudgetApiHelperImpl @Inject constructor(private val apiService: BudgetApiService) : BudgetApiHelper {

    override suspend fun getBudgets(): Response<Budget> {
        return apiService.getBudget(MonthSelector.month)
    }

    override suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse> {
        return apiService.updateBudget(updateBudgetRequest)
    }

    override suspend fun recalculateBudgets(): Response<Budget> {
        return apiService.recalculateBudgets(MonthSelector.month)
    }

    override suspend fun getBudgetItems(budgetId: Int): Response<List<InvoiceDetails>> {
        return apiService.getBudgetItems(budgetId)
    }
}

interface BudgetApiHelper {

    suspend fun getBudgets(): Response<Budget>
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>
    suspend fun recalculateBudgets(): Response<Budget>
    suspend fun getBudgetItems(budgetId: Int): Response<List<InvoiceDetails>>
}