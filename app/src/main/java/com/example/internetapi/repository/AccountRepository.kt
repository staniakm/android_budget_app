package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.UpdateAccountRequest
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getAccounts() = apiHelper.getAccounts()
    suspend fun getAccountInvoices(accountId: Int) = apiHelper.getAccountInvoices(accountId)

    suspend fun getAccountIncome(accountId: Int) = apiHelper.getAccountIncome(accountId)
    suspend fun updateAccount(accountId: Int, updateAccountRequest: UpdateAccountRequest) = apiHelper.updateAccount(accountId, updateAccountRequest)
    suspend fun addAccountIncome(request: AccountIncomeRequest) = apiHelper.addAccountIncome(request)
}