package com.example.internetapi.api

import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface MediaApiService {

    @GET("media/type/all")
    suspend fun getMediaTypes(): Response<List<MediaType>>

    @POST("media/type")
    suspend fun addNewMediaType(@Body mediaTypeRequest: MediaTypeRequest): Response<MediaType>

    @GET("media/usage/{mediaTypeId}")
    suspend fun getMediaUsageByType(@Path("mediaTypeId") mediaTypeId: Int): Response<List<MediaUsage>>

    @POST("media/usage")
    suspend fun addMediaUsage(@Body mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>>

    @DELETE("media/usage/{mediaUsageId}")
    suspend fun removeMediaUsageItem(@Path("mediaUsageId") id: Int): Response<Void>
}

class MediaApiHelperImpl @Inject constructor(private val apiService: MediaApiService) : MediaApiHelper {

    override suspend fun getMediaTypes(): Response<List<MediaType>> {
        return apiService.getMediaTypes()
    }

    override suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType> {
        return apiService.addNewMediaType(mediaTypeRequest)
    }

    override suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>> {
        return apiService.getMediaUsageByType(mediaTypeId)
    }

    override suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>> {
        return apiService.addMediaUsage(mediaUsageRequest)
    }

    override suspend fun removeMediaUsageItem(id: Int): Response<Void> {
        return apiService.removeMediaUsageItem(id)
    }
}

interface MediaApiHelper {

    suspend fun getMediaTypes(): Response<List<MediaType>>
    suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType>
    suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>>
    suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>>
    suspend fun removeMediaUsageItem(id: Int): Response<Void>
}