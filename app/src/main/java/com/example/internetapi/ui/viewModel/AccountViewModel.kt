package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.*
import com.example.internetapi.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(private val accountRepository: AccountRepository) :
    ViewModelDataFunction() {

    fun accountInvoices(accountId: Int): LiveData<Resource<List<AccountInvoice>>> {
        return executeLiveDataList { accountRepository.getAccountInvoices(accountId) }
    }

    fun getAccounts(): MutableLiveData<Resource<List<Account>>> {
        return executeLiveDataList { accountRepository.getAccounts() }
    }


    fun getAccountIncome(accountId: Int): LiveData<Resource<List<AccountIncome>>> {
        return executeLiveDataList { accountRepository.getAccountIncome(accountId) }
    }

    fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): LiveData<Resource<UpdateAccountResponse>> {
        return executeLiveDataSingle {
            accountRepository.updateAccount(
                accountId,
                updateAccountRequest
            )
        }
    }

    fun addIncome(request: AccountIncomeRequest) {
        executeLiveDataSingle { accountRepository.addAccountIncome(request) }

    }

    fun getIncomeTypes(): LiveData<Resource<List<IncomeType>>> {
        return executeLiveDataList { accountRepository.getIncomeTypes() }
    }

    fun transferMoney(request: TransferMoneyRequest): LiveData<Resource<UpdateAccountResponse>> {
        return executeLiveDataSingle { accountRepository.transferMoney(request) }
    }
}