package com.example.internetapi.functions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

abstract class ViewModelDataFunction : ViewModel() {
    fun <T> executeLiveDataList(repoFun: suspend () -> Response<List<T>>): LiveData<Resource<List<T>>> {
        val data = MutableLiveData<Resource<List<T>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            try {
                repoFun.invoke().let {
                    if (it.isSuccessful) {
                        data.postValue(Resource.success(it.body()))
                    } else {
                        data.postValue(Resource.error(it.errorBody()?.toString() ?: "Unknown error", null))
                    }
                }
            } catch (e: Exception) {
                data.postValue(Resource.error(e.message ?: "Unknown error", null))
            }
        }
        return data
    }

    fun <T> executeLiveDataSingle(function: suspend () -> Response<T>): LiveData<Resource<T>> {
        val data = MutableLiveData<Resource<T>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            try {
                function.invoke().let {
                    if (it.isSuccessful) {
                        data.postValue(Resource.success(it.body()))
                    } else {
                        data.postValue(Resource.error(it.errorBody()?.toString() ?: "Unknown error", null))
                    }
                }
            } catch (e: Exception) {
                data.postValue(Resource.error(e.message ?: "Unknown error", null))
            }
        }
        return data
    }
}
