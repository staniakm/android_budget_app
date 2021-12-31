package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.internetapi.api.Resource
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
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

    fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): LiveData<Resource<MediaType>> {
        val data = MutableLiveData<Resource<MediaType>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mediaRepository.addNewMediaType(mediaTypeRequest)
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

    fun getMediaUsageByType(mediaTypeId: Int): LiveData<Resource<List<MediaUsage>>> {
        val data = MutableLiveData<Resource<List<MediaUsage>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mediaRepository.getMediaUsageByType(mediaTypeId)
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

    fun addMediaUsageEntry(mediaRegisterRequest: MediaRegisterRequest): LiveData<Resource<List<MediaUsage>>> {
        val data = MutableLiveData<Resource<List<MediaUsage>>>()
        viewModelScope.launch {
            data.postValue(Resource.loading(null))
            mediaRepository.addMediaUsageEntry(mediaRegisterRequest)
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