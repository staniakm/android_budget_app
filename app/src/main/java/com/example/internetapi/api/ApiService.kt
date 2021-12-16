package com.example.internetapi.api

import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface ApiService {
    @GET("account")
    suspend fun getAccounts(@Query("month") month: Int): Response<List<Account>>

    @GET("account/{accountId}")
    suspend fun getAccountInvoices(
        @Path("accountId") account: Long,
        @Query("month") month: Int
    ): Response<List<AccountInvoice>>

    @GET("account/{accountId}/income")
    suspend fun getAccountIncome(
        @Path("accountId") account: Long,
        @Query("month") month: Int
    ): Response<List<AccountIncome>>


    @GET("invoice/{invoiceId}")
    suspend fun getInvoiceDetails(@Path("invoiceId") invoiceId: Long): Response<List<InvoiceDetails>>

    @GET("budget")
    suspend fun getBudget(@Query("month") month: Int): Response<Budget>

    @PUT("budget")
    suspend fun updateBudget(
        @Query("month") month: Int,
        @Body updateBudgetRequest: UpdateBudgetRequest
    ): Response<Unit>
}

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getAccounts(): Response<List<Account>> =
        apiService.getAccounts(MonthSelector.month)

    override suspend fun getAccountInvoices(accountId: Long): Response<List<AccountInvoice>> =
        apiService.getAccountInvoices(accountId, MonthSelector.month)

    override suspend fun getAccountIncome(accountId: Long): Response<List<AccountIncome>> =
        apiService.getAccountIncome(accountId, MonthSelector.month)

    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> =
        apiService.getInvoiceDetails(invoiceId)

    override suspend fun getBudgets(): Response<Budget> {
        return apiService.getBudget(MonthSelector.month)
    }

    override suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<Unit> {
        return apiService.updateBudget(MonthSelector.month, updateBudgetRequest)
    }


}

interface ApiHelper {
    suspend fun getAccounts(): Response<List<Account>>
    suspend fun getAccountInvoices(accountId: Long): Response<List<AccountInvoice>>
    suspend fun getAccountIncome(accountId: Long): Response<List<AccountIncome>>
    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
    suspend fun getBudgets(): Response<Budget>
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<Unit>
}