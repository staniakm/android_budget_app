package com.example.internetapi.repository

import com.example.internetapi.api.ShopApiHelper
import com.example.internetapi.models.CreateShopItemRequest
import com.example.internetapi.models.CreateShopRequest
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val apiHelper: ShopApiHelper
) {
    suspend fun getShops() = apiHelper.getShops()

    suspend fun getShopItems(shopId: Int) = apiHelper.getShopItems(shopId)
    suspend fun createShop(shopRequest: CreateShopRequest) = apiHelper.createShop(shopRequest)
    suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest) =
        apiHelper.createNewShopItem(createShopItemRequest)
}