package com.example.internetapi.repository

import com.example.internetapi.api.InvoiceApiHelper
import com.example.internetapi.models.NewInvoiceRequest
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import retrofit2.Response
import javax.inject.Inject

class InvoiceRepository @Inject constructor(
    private val apiHelper: InvoiceApiHelper
) {
    suspend fun getInvoiceDetails(invoiceId: Long) = apiHelper.getInvoiceDetails(invoiceId)
    suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest) =
        apiHelper.updateInvoiceAccount(updateInvoiceAccountRequest)

    suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest) =
        apiHelper.createNewInvoice(newInvoiceRequest)

    suspend fun deleteInvoice(listId: Long): Response<Void> {
        return apiHelper.deleteInvoice(listId)
    }
}