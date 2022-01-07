package com.example.internetapi.api

import com.example.internetapi.constant.Constant.SHOP
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface ShopApiService {

    @GET("$SHOP/all")
    suspend fun getAllShops(): Response<List<Shop>>

    @GET("$SHOP/{shopId}")
    suspend fun getShopItems(@Path("shopId") shopId: Int): Response<List<ShopItem>>

    @POST(SHOP)
    suspend fun createShop(@Body name: CreateShopRequest): Response<Shop>

    @POST("$SHOP/newItem")
    suspend fun createNewShopItem(@Body createShopItemRequest: CreateShopItemRequest): Response<ShopItem>
}

class ShopApiHelperImpl @Inject constructor(private val apiService: ShopApiService) :
    ShopApiHelper {

    override suspend fun getShops(): Response<List<Shop>> {
        return apiService.getAllShops()
    }

    override suspend fun getShopItems(shopId: Int): Response<List<ShopItem>> {
        return apiService.getShopItems(shopId)
    }

    override suspend fun createShop(shopRequest: CreateShopRequest): Response<Shop> {
        return apiService.createShop(shopRequest)
    }

    override suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest): Response<ShopItem> {
        return apiService.createNewShopItem(createShopItemRequest)
    }
}

interface ShopApiHelper {
    suspend fun getShops(): Response<List<Shop>>
    suspend fun getShopItems(shopId: Int): Response<List<ShopItem>>
    suspend fun createShop(shopRequest: CreateShopRequest): Response<Shop>
    suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest): Response<ShopItem>
}