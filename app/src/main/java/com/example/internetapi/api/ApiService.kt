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
        @Path("accountId") account: Int,
        @Query("month") month: Int
    ): Response<List<AccountInvoice>>

    @GET("account/{accountId}/income")
    suspend fun getAccountIncome(
        @Path("accountId") account: Int,
        @Query("month") month: Int
    ): Response<List<AccountIncome>>

    @PUT("account/{accountId}")
    suspend fun updateAccount(
        @Path("accountId") accountId: Int,
        @Body updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse>


    @GET("invoice/{invoiceId}")
    suspend fun getInvoiceDetails(@Path("invoiceId") invoiceId: Long): Response<List<InvoiceDetails>>

    @GET("budget")
    suspend fun getBudget(@Query("month") month: Int): Response<Budget>

    @PUT("budget")
    suspend fun updateBudget(@Body updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>
}

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getAccounts(): Response<List<Account>> =
        apiService.getAccounts(MonthSelector.month)

    override suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>> =
        apiService.getAccountInvoices(accountId, MonthSelector.month)

    override suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>> =
        apiService.getAccountIncome(accountId, MonthSelector.month)

    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> =
        apiService.getInvoiceDetails(invoiceId)

    override suspend fun getBudgets(): Response<Budget> {
        return apiService.getBudget(MonthSelector.month)
    }

    override suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse> {
        return apiService.updateBudget(updateBudgetRequest)
    }

    override suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse> {
        return apiService.updateAccount(accountId, updateAccountRequest)
    }


}

interface ApiHelper {
    suspend fun getAccounts(): Response<List<Account>>
    suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>>
    suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>>
    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
    suspend fun getBudgets(): Response<Budget>
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>
    suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse>
}