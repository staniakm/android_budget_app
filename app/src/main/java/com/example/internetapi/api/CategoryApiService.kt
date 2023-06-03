package com.example.internetapi.api

import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.*
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

interface CategoryApiService {

    @GET("category")
    suspend fun getCategories(@Query("month") month: Int): Response<List<Category>>

    @GET("category/{categoryId}/details")
    suspend fun getCategoryDetails(@Path("categoryId") categoryId: Int, @Query("month") month: Int): Response<List<CategoryDetails>>
}

class CategoryApiHelperImpl @Inject constructor(private val apiService: CategoryApiService) :
    CategoryApiHelper {

    override suspend fun getCategories(): Response<List<Category>> {
        return apiService.getCategories(MonthSelector.month)
    }

    override suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>> {
        return apiService.getCategoryDetails(categoryId, MonthSelector.month)
    }
}

interface CategoryApiHelper {

    suspend fun getCategories(): Response<List<Category>>
    suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>>
}