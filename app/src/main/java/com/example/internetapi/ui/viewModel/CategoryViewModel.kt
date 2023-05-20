package com.example.internetapi.ui.viewModel

import androidx.lifecycle.LiveData
import com.example.internetapi.api.Resource
import com.example.internetapi.functions.ViewModelDataFunction
import com.example.internetapi.models.Category
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.repository.CategoryRepository
import com.example.internetapi.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(private val mediaRepository: CategoryRepository) :
    ViewModelDataFunction() {

    fun getCategories(): LiveData<Resource<List<Category>>> {
        return executeLiveDataList { mediaRepository.getCategories() }
    }
}