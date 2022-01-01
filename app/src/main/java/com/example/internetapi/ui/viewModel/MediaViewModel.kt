package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
    ViewModelDataFunction() {

    fun getMediaTypes(): LiveData<Resource<List<MediaType>>> {
        return executeLiveDataList { mediaRepository.getMediaTypes() }
    }

    fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): LiveData<Resource<MediaType>> {
        return executeLiveDataSingle { mediaRepository.addNewMediaType(mediaTypeRequest) }
    }

    fun getMediaUsageByType(mediaTypeId: Int): LiveData<Resource<List<MediaUsage>>> {
        return executeLiveDataList { mediaRepository.getMediaUsageByType(mediaTypeId) }
    }

    fun addMediaUsageEntry(mediaRegisterRequest: MediaRegisterRequest): LiveData<Resource<List<MediaUsage>>> {
        return executeLiveDataList { mediaRepository.addMediaUsageEntry(mediaRegisterRequest) }
    }

    fun removeMediaUsage(id: Int): LiveData<Resource<Void>> {
        return executeLiveDataSingle { mediaRepository.removeUsageItem(id) }
    }
}