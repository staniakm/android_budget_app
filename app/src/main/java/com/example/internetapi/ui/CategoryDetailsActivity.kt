package com.example.internetapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityCategoryDetailsBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.CategoryDetailsAdapter
import com.example.internetapi.ui.viewModel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_LOAD_CATEGORY_DETAILS = "Failed to load category details data"

    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var binding: ActivityCategoryDetailsBinding
    private lateinit var adapter: CategoryDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CategoryDetailsAdapter(viewModel, this)
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        intent.extras?.let { extra ->
            val categoryId = extra.getInt("categoryId")
            extra.getString("name")
            loadData(categoryId)
        }
    }

    private fun loadData(categoryId: Int) {
        viewModel.getCategoryDetails(categoryId).observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_CATEGORY_DETAILS)
                Status.LOADING -> {}
            }
        }
    }

    private fun processSuccess(it: Resource<List<CategoryDetails>>) {
        it.data.let { res ->
            res?.let { list ->
                adapter.submitList(list)
            }
        }
    }
}