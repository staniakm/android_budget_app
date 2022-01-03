package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.TransferMoneyRequest
import com.example.internetapi.models.UpdateAccountRequest
import javax.inject.Inject

class ShopRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getShops() = apiHelper.getShops()

    suspend fun getShopItems(shopId: Int) = apiHelper.getShopItems(shopId)
}