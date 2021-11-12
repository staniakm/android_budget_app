package com.example.internetapi.api

import com.example.internetapi.models.Account
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.InvoiceDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject

interface ApiService {
    @GET("account")
    suspend fun getAccounts(): Response<List<Account>>

    @GET("account/{accountId}")
    suspend fun getAccountInvoices(@Path("accountId") account: Long): Response<List<AccountInvoice>>

    @GET("account/{accountId}/income")
    suspend fun getAccountIncome(@Path("accountId") account: Long): Response<List<AccountIncome>>


    @GET("invoice/{invoiceId}")
    suspend fun getInvoiceDetails(@Path("invoiceId") invoiceId: Long): Response<List<InvoiceDetails>>
}

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getAccounts(): Response<List<Account>> = apiService.getAccounts()
    override suspend fun getAccountInvoices(accountId: Long): Response<List<AccountInvoice>> =
        apiService.getAccountInvoices(accountId)
    override suspend fun getAccountIncome(accountId: Long): Response<List<AccountIncome>> =
        apiService.getAccountIncome(accountId)

    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> =
        apiService.getInvoiceDetails(invoiceId)

}

interface ApiHelper {
    suspend fun getAccounts(): Response<List<Account>>
    suspend fun getAccountInvoices(accountId: Long): Response<List<AccountInvoice>>
    suspend fun getAccountIncome(accountId: Long): Response<List<AccountIncome>>
    suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>>
}