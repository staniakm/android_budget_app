package com.example.internetapi.functions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.UpdateAccountResponse
import kotlinx.coroutines.launch
import retrofit2.Response

abstract class ViewModelDataFunction : ViewModel() {
    fun <T> executeLiveDataList(repoFun: suspend () -> Response<List<T>>): MutableLiveData<Resource<List<T>>> {
        val data = MutableLiveData<Resource<List<T>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            repoFun.invoke()
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

    fun <T> executeLiveDataSingle(function: suspend () -> Response<T>): LiveData<Resource<T>> {
        val data = MutableLiveData<Resource<T>>()
        viewModelScope.launch {
            function.invoke()
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