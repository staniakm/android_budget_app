package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.CreateShopRequest
import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem
import com.example.internetapi.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountOutcomeViewModel @Inject constructor(private val shopRepository: ShopRepository) :
    ViewModelDataFunction() {

    fun getShops(): LiveData<Resource<List<Shop>>> {
        return executeLiveDataList { shopRepository.getShops() }
    }

    fun getShopItems(shopId: Int): LiveData<Resource<List<ShopItem>>> {
        return executeLiveDataList { shopRepository.getShopItems(shopId) }
    }

    fun createShop(name: String): LiveData<Resource<Shop>> {

        return executeLiveDataSingle { shopRepository.createShop(CreateShopRequest(name)) }
    }
}