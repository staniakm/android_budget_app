package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.*
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getMediaTypes() = apiHelper.getMediaTypes()
    suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest) = apiHelper.addNewMediaType(mediaTypeRequest)
    suspend fun getMediaUsageByType(mediaTypeId: Int) = apiHelper.getMediaUsageByType(mediaTypeId)
    suspend fun addMediaUsageEntry(mediaRegisterRequest: MediaRegisterRequest) = apiHelper.addMediaUsageEntry(mediaRegisterRequest)
    suspend fun removeUsageItem(id:Int) = apiHelper.removeMediaUsageItem(id)
}