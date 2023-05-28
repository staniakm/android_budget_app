package com.example.internetapi.api

import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface CategoryApiService {

    @GET("category")
    suspend fun getCategories(): Response<List<Category>>

    @GET("category/{categoryId}/details")
    suspend fun getCategoryDetails(@Path("categoryId") budgetId: Int): Response<List<CategoryDetails>>
}

class CategoryApiHelperImpl @Inject constructor(private val apiService: CategoryApiService) :
    CategoryApiHelper {

    override suspend fun getCategories(): Response<List<Category>> {
        return apiService.getCategories()
    }

    override suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>> {
        return apiService.getCategoryDetails(categoryId)
    }
}

interface CategoryApiHelper {

    suspend fun getCategories(): Response<List<Category>>
    suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>>
}