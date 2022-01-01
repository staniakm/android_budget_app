package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(private val invoiceRepository: InvoiceRepository) :
    ViewModelDataFunction() {

    fun invoiceDetails(invoiceId: Long): LiveData<Resource<List<InvoiceDetails>>> {
        return executeLiveDataList { invoiceRepository.getInvoiceDetails(invoiceId) }
    }

    fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): LiveData<Resource<AccountInvoice>> {
        return executeLiveDataSingle {
            invoiceRepository.updateInvoiceAccount(
                updateInvoiceAccountRequest
            )
        }
    }
}