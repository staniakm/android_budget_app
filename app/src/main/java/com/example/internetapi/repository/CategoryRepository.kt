package com.example.internetapi.repository

import com.example.internetapi.api.CategoryApiHelper
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val apiHelper: CategoryApiHelper
) {
    suspend fun getCategories() = apiHelper.getCategories()
    suspend fun getCategoryDetails(categoryId: Int) = apiHelper.getCategoryDetails(categoryId)
}