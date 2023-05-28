package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.Category
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(private val categoryRepository: CategoryRepository) :
    ViewModelDataFunction() {

    fun getCategories(): LiveData<Resource<List<Category>>> {
        return executeLiveDataList { categoryRepository.getCategories() }
    }

    fun getCategoryDetails(categoryId: Int): LiveData<Resource<List<CategoryDetails>>> {
        return executeLiveDataList { categoryRepository.getCategoryDetails(categoryId) }
    }
}