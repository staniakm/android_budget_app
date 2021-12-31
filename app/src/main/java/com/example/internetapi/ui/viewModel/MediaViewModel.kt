package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.MediaType
import com.example.internetapi.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
    ViewModel() {

    fun getMediaTypes(): LiveData<Resource<List<MediaType>>> {
        val data = MutableLiveData<Resource<List<MediaType>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mediaRepository.getMediaTypes()
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