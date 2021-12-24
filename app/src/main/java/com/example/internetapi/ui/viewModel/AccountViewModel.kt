package com.example.internetapi.ui.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.*
import com.example.internetapi.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(private val accountRepository: AccountRepository) :
    ViewModel() {

    fun accountInvoices(accountId: Int): LiveData<Resource<List<AccountInvoice>>> {
        val data = MutableLiveData<Resource<List<AccountInvoice>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            accountRepository.getAccountInvoices(accountId)
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

    fun getAccounts(): MutableLiveData<Resource<List<Account>>> {
        val data = MutableLiveData<Resource<List<Account>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            accountRepository.getAccounts()
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

    fun getAccountIncome(accountId: Int): LiveData<Resource<List<AccountIncome>>> {
        val data = MutableLiveData<Resource<List<AccountIncome>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            accountRepository.getAccountIncome(accountId)
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

    fun updateAccount(
        accountId: Int,
        updateAccountRequest: UpdateAccountRequest
    ): MutableLiveData<Resource<UpdateAccountResponse>> {
        val data = MutableLiveData<Resource<UpdateAccountResponse>>()
        viewModelScope.launch {
            accountRepository.updateAccount(accountId, updateAccountRequest)
                .let {
                    if (it.isSuccessful) {
                        Log.i("TAG", "updateAccount: SUCCESS")
                        data.postValue(Resource.success(it.body()))
                    } else {
                        Log.e("TAG", "updateAccount: FAILURE")
                        data.postValue(Resource.error(it.errorBody().toString(), null))
                    }
                }
        }
        return data
    }

    fun addIncome(request: AccountIncomeRequest) {
        viewModelScope.launch {
            accountRepository.addAccountIncome(request)
                .let {
                    if(it.isSuccessful){
                        Log.e("TAG", "updateAccount: SUCCESS")
                    }else {
                        Log.e("TAG", "updateAccount: FAILURE")
                    }
                }
        }
    }
}