package com.example.internetapi.api

import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject
interface ApiService {


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

    @POST("shop")
    suspend fun createShop(@Body name: CreateShopRequest): Response<Shop>

    @POST("invoice")
    suspend fun createNewInvoice(@Body newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse>

    @POST("shop/newItem")
    suspend fun createNewShopItem(@Body createShopItemRequest: CreateShopItemRequest): Response<ShopItem>
}

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {


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


    override suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice> {
        return apiService.updateInvoiceAccount(
            updateInvoiceAccountRequest.invoiceId,
            updateInvoiceAccountRequest
        )
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

    override suspend fun createShop(shopRequest: CreateShopRequest): Response<Shop> {
        return apiService.createShop(shopRequest)
    }

    override suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse> {
        return apiService.createNewInvoice(newInvoiceRequest)
    }

    override suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest): Response<ShopItem> {
        return apiService.createNewShopItem(createShopItemRequest)
    }
}

interface ApiHelper {

    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
    suspend fun getBudgets(): Response<Budget>
    suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse>
    suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice>
    suspend fun recalculateBudgets(): Response<Budget>
    suspend fun getMediaTypes(): Response<List<MediaType>>
    suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType>
    suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>>
    suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>>
    suspend fun removeMediaUsageItem(id: Int): Response<Void>
    suspend fun getShops(): Response<List<Shop>>
    suspend fun getShopItems(shopId: Int): Response<List<ShopItem>>
    suspend fun createShop(shopRequest: CreateShopRequest): Response<Shop>
    suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse>
    suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest): Response<ShopItem>
}