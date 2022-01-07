package com.example.internetapi.api

import com.example.internetapi.constant.Constant.ACCOUNT
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
interface AccountApiService {
    @GET(ACCOUNT)
    suspend fun getAccounts(@Query("month") month: Int): Response<List<Account>>

    @GET("$ACCOUNT/{accountId}")
    suspend fun getAccountInvoices(
        @Path("accountId") account: Int,
        @Query("month") month: Int
    ): Response<List<AccountInvoice>>

    @GET("$ACCOUNT/{accountId}/income")
    suspend fun getAccountIncome(
        @Path("accountId") account: Int,
        @Query("month") month: Int
    ): Response<List<AccountIncome>>

    @PUT("$ACCOUNT/{accountId}")
    suspend fun updateAccount(
        @Path("accountId") accountId: Int,
        @Body updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse>

    @POST("$ACCOUNT/{accountId}")
    suspend fun addAccountIncome(
        @Path("accountId") accountId: Int,
        @Body request: AccountIncomeRequest
    ): Response<List<AccountIncome>>

    @PUT("$ACCOUNT/{accountId}/transfer")
    suspend fun transferMoney(
        @Path("accountId") accountId: Int,
        @Body request: TransferMoneyRequest
    ): Response<UpdateAccountResponse>

    @GET("$ACCOUNT/income/type")
    suspend fun getIncomeTypes(): Response<List<IncomeType>>

}

class AccountApiHelperImpl @Inject constructor(private val apiService: AccountApiService) : AccountApiHelper {
    override suspend fun getAccounts(): Response<List<Account>> =
        apiService.getAccounts(MonthSelector.month)

    override suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>> =
        apiService.getAccountInvoices(accountId, MonthSelector.month)

    override suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>> =
        apiService.getAccountIncome(accountId, MonthSelector.month)

    override suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse> {
        return apiService.updateAccount(accountId, updateAccountRequest)
    }
    override suspend fun addAccountIncome(request: AccountIncomeRequest): Response<List<AccountIncome>> {
        return apiService.addAccountIncome(request.accountId, request)
    }

    override suspend fun getIncomeTypes(): Response<List<IncomeType>> {
        return apiService.getIncomeTypes()
    }

    override suspend fun transferMoney(request: TransferMoneyRequest): Response<UpdateAccountResponse> {
        return apiService.transferMoney(request.accountId, request)
    }

}

interface AccountApiHelper {
    suspend fun getAccounts(): Response<List<Account>>
    suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>>
    suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>>
    suspend fun addAccountIncome(request: AccountIncomeRequest): Response<List<AccountIncome>>
    suspend fun transferMoney(request: TransferMoneyRequest): Response<UpdateAccountResponse>
    suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse>
    suspend fun getIncomeTypes(): Response<List<IncomeType>>

}
