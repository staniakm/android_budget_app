package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getAccounts() = apiHelper.getAccounts()
    suspend fun getAccountInvoices(accountId: Long) = apiHelper.getAccountInvoices(accountId)
    suspend fun getInvoiceDetails(invoiceId: Long) = apiHelper.getInvoiceDetails(invoiceId)
    suspend fun getAccountIncome(accountId: Long) = apiHelper.getAccountIncome(accountId)
}