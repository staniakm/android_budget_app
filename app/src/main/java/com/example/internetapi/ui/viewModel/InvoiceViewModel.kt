package com.example.internetapi.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val invoiceRepository: InvoiceRepository) :
    ViewModel() {

    fun invoiceDetails(invoiceId: Long): LiveData<Resource<List<InvoiceDetails>>> {
        val data = MutableLiveData<Resource<List<InvoiceDetails>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            invoiceRepository.getInvoiceDetails(invoiceId)
                .let {
                    if (it.isSuccessful) {
                        data.postValue(Resource.success(it.body()))
                    } else {
                        data.postValue(Resource.error(it.errorBody().toString(), null))
                    }
                }
        }
        return data
    }

    fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest) {
        viewModelScope.launch {
            invoiceRepository.updateInvoiceAccount(updateInvoiceAccountRequest)
                .let {
                    if (it.isSuccessful) {
                        Log.e("TAG", "updateInvoiceAccount: ")
                    }
                }
        }
    }
}