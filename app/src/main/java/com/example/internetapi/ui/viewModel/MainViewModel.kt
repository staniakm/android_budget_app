package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Account
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mainRepository: AccountRepository) :
    ViewModel() {

    fun accountInvoices(accountId: Long): LiveData<Resource<List<AccountInvoice>>> {
        val data = MutableLiveData<Resource<List<AccountInvoice>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mainRepository.getAccountInvoices(accountId)
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
            mainRepository.getAccounts()
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

    fun getAccountIncome(accountId: Long): LiveData<Resource<List<AccountIncome>>> {
        val data = MutableLiveData<Resource<List<AccountIncome>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mainRepository.getAccountIncome(accountId)
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
}