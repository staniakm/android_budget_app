package com.example.internetapi.ui.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Budget
import com.example.internetapi.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(private val repository: BudgetRepository) :
    ViewModel() {

    fun getBudgets(): MutableLiveData<Resource<Budget>> {
        val data = MutableLiveData<Resource<Budget>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            repository.getBudgets()
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