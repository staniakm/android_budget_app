package com.example.internetapi.repository

import com.example.internetapi.api.ApiHelper
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.TransferMoneyRequest
import com.example.internetapi.models.UpdateAccountRequest
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val apiHelper: ApiHelper
) {
    suspend fun getMediaTypes() = apiHelper.getMediaTypes()
    suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest) = apiHelper.addNewMediaType(mediaTypeRequest)
    suspend fun getMediaUsageByType(mediaTypeId: Int) = apiHelper.getMediaUsageByType(mediaTypeId)
}