package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import javax.inject.Inject

class InvoiceRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getInvoiceDetails(invoiceId: Long) = apiHelper.getInvoiceDetails(invoiceId)
    suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest) = apiHelper.updateInvoiceAccount(updateInvoiceAccountRequest)
}