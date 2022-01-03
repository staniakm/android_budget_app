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


    @POST("account/{accountId}")
    suspend fun addAccountIncome(
        @Path("accountId") accountId: Int,
        @Body request: AccountIncomeRequest
    ): Response<List<AccountIncome>>

    @PUT("account/{accountId}/transfer")
    suspend fun transferMoney(
        @Path("accountId") accountId: Int,
        @Body request: TransferMoneyRequest
    ): Response<UpdateAccountResponse>

    @GET("invoice/{invoiceId}")
    suspend fun getInvoiceDetails(@Path("invoiceId") invoiceId: Long): Response<List<InvoiceDetails>>

    @GET("budget")
    suspend fun getBudget(@Query("month") month: Int): Response<Budget>

    @PUT("budget")
    suspend fun updateBudget(@Body updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>

    @PUT("budget/recalculate")
    suspend fun recalculateBudgets(@Query("month") month: Int): Response<Budget>

    @PUT("invoice/{invoiceId}")
    suspend fun updateInvoiceAccount(
        @Path("invoiceId") invoiceId: Long,
        @Body updateInvoiceAccountRequest: UpdateInvoiceAccountRequest
    ): Response<AccountInvoice>

    @GET("account/income/type")
    suspend fun getIncomeTypes(): Response<List<IncomeType>>

    @GET("media/type/all")
    suspend fun getMediaTypes(): Response<List<MediaType>>

    @POST("media/type")
    suspend fun addNewMediaType(@Body mediaTypeRequest: MediaTypeRequest): Response<MediaType>

    @GET("media/usage/{mediaTypeId}")
    suspend fun getMediaUsageByType(@Path("mediaTypeId") mediaTypeId: Int): Response<List<MediaUsage>>

    @POST("media/usage")
    suspend fun addMediaUsage(@Body mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>>

    @DELETE("media/usage/{mediaUsageId}")
    suspend fun removeMediaUsageItem(@Path("mediaUsageId") id: Int): Response<Void>

    @GET("shop/all")
    suspend fun getAllShops(): Response<List<Shop>>

    @GET("shop/{shopId}")
    suspend fun getShopItems(@Path("shopId") shopId: Int): Response<List<ShopItem>>
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

    override suspend fun recalculateBudgets(): Response<Budget> {
        return apiService.recalculateBudgets(MonthSelector.month)
    }

    override suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse> {
        return apiService.updateAccount(accountId, updateAccountRequest)
    }

    override suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice> {
        return apiService.updateInvoiceAccount(
            updateInvoiceAccountRequest.invoiceId,
            updateInvoiceAccountRequest
        )
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

    override suspend fun getMediaTypes(): Response<List<MediaType>> {
        return apiService.getMediaTypes()
    }

    override suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType> {
        return apiService.addNewMediaType(mediaTypeRequest)
    }

    override suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>> {
        return apiService.getMediaUsageByType(mediaTypeId)
    }

    override suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>> {
        return apiService.addMediaUsage(mediaUsageRequest)
    }

    override suspend fun removeMediaUsageItem(id: Int): Response<Void> {
        return apiService.removeMediaUsageItem(id)
    }

    override suspend fun getShops(): Response<List<Shop>> {
        return apiService.getAllShops()
    }

    override suspend fun getShopItems(shopId: Int): Response<List<ShopItem>> {
        return apiService.getShopItems(shopId)
    }
}

interface ApiHelper {
    suspend fun getAccounts(): Response<List<Account>>
    suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>>
    suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>>
    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
    suspend fun getBudgets(): Response<Budget>
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>
    suspend fun recalculateBudgets(): Response<Budget>

    suspend fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): Response<UpdateAccountResponse>

    suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice>
    suspend fun addAccountIncome(request: AccountIncomeRequest): Response<List<AccountIncome>>
    suspend fun getIncomeTypes(): Response<List<IncomeType>>

    suspend fun transferMoney(request: TransferMoneyRequest): Response<UpdateAccountResponse>
    suspend fun getMediaTypes(): Response<List<MediaType>>
    suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType>
    suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>>
    suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>>
    suspend fun removeMediaUsageItem(id: Int): Response<Void>
    suspend fun getShops(): Response<List<Shop>>
    suspend fun getShopItems(shopId: Int): Response<List<ShopItem>>
}