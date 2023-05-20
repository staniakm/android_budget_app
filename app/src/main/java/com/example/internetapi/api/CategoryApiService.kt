package com.example.internetapi.api

import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface CategoryApiService {

    @GET("category")
    suspend fun getCategories(): Response<List<Category>>
}

class CategoryApiHelperImpl @Inject constructor(private val apiService: CategoryApiService) :
    CategoryApiHelper {

    override suspend fun getCategories(): Response<List<Category>> {
        return apiService.getCategories()
    }
}

interface CategoryApiHelper {

    suspend fun getCategories(): Response<List<Category>>
}