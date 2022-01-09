package com.example.internetapi.repository

import com.example.internetapi.api.AccountApiHelper
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.TransferMoneyRequest
import com.example.internetapi.models.UpdateAccountRequest
import javax.inject.Inject

class AccountRepository @Inject constructor(
    private val apiHelper: AccountApiHelper
) {
    suspend fun getAccounts() = apiHelper.getAccounts()
    suspend fun getAccountInvoices(accountId: Int) = apiHelper.getAccountInvoices(accountId)

    suspend fun getAccountIncome(accountId: Int) = apiHelper.getAccountIncome(accountId)
    suspend fun updateAccount(accountId: Int, updateAccountRequest: UpdateAccountRequest) =
        apiHelper.updateAccount(accountId, updateAccountRequest)

    suspend fun addAccountIncome(request: AccountIncomeRequest) =
        apiHelper.addAccountIncome(request)

    suspend fun getIncomeTypes() = apiHelper.getIncomeTypes()
    suspend fun transferMoney(request: TransferMoneyRequest) = apiHelper.transferMoney(request)
    suspend fun getOperations(accountId: Int) = apiHelper.getAccountOperations(accountId)
}