package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.CreateShopRequest
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getShops() = apiHelper.getShops()

    suspend fun getShopItems(shopId: Int) = apiHelper.getShopItems(shopId)
    suspend fun createShop(shopRequest: CreateShopRequest) = apiHelper.createShop(shopRequest)
}