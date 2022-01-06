package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.*
import com.example.internetapi.repository.InvoiceRepository
import com.example.internetapi.repository.ShopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountOutcomeViewModel @Inject constructor(private val shopRepository: ShopRepository, private val invoiceRepository: InvoiceRepository) :
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

    fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): LiveData<Resource<AccountInvoice>> {
        return executeLiveDataSingle { invoiceRepository.createNewInvoice(newInvoiceRequest) }
    }

    fun createNewShopItem(shopId: Int, name: String): LiveData<Resource<ShopItem>> {
        return executeLiveDataSingle { shopRepository.createNewShopItem(CreateShopItemRequest(shopId, name)) }
    }
}