package com.example.internetapi.api

import com.example.internetapi.constant.Constant.INVOICE
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface InvoiceApiService {


    @GET("$INVOICE/{invoiceId}")
    suspend fun getInvoiceDetails(@Path("invoiceId") invoiceId: Long): Response<List<InvoiceDetails>>

    @PUT("$INVOICE/{invoiceId}")
    suspend fun updateInvoiceAccount(
        @Path("invoiceId") invoiceId: Long,
        @Body updateInvoiceAccountRequest: UpdateInvoiceAccountRequest
    ): Response<AccountInvoice>

    @POST(INVOICE)
    suspend fun createNewInvoice(@Body newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse>

}

class InvoiceApiHelperImpl @Inject constructor(private val apiService: InvoiceApiService) :
    InvoiceApiHelper {


    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> =
        apiService.getInvoiceDetails(invoiceId)

    override suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice> {
        return apiService.updateInvoiceAccount(
            updateInvoiceAccountRequest.invoiceId,
            updateInvoiceAccountRequest
        )
    }

    override suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse> {
        return apiService.createNewInvoice(newInvoiceRequest)
    }

}

interface InvoiceApiHelper {

    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
    suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice>
    suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse>
}