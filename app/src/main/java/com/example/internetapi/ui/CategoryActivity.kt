package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityCategoryBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.Category
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.CategoryAdapter
import com.example.internetapi.ui.adapters.OnItemClickedListener
import com.example.internetapi.ui.viewModel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity(), OnItemClickedListener {
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CategoryAdapter(this)
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        loadData()
    }

    private fun loadData() {
        viewModel.getCategories().observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, "Failed to load media types data")
                Status.LOADING -> Log.println(Log.DEBUG, "MediaType", "Loading.....")
            }
        }
    }

    private fun processSuccess(it: Resource<List<Category>>) {
        it.data?.let {
            if (it.isEmpty()) {
                errorSnackBar(binding.root, "No data available. Please add new data")
            } else {
                adapter.submitList(it)
            }
        }
    }

    override fun onClick(position: Int, element: String) {
        val item = adapter.getItem(position)

        when (element) {
            "layout" ->
                Intent(this, CategoryDetailsActivity::class.java)
                    .apply {
                        this.putExtra("name", item.name)
                        this.putExtra("categoryId", item.id)
                    }.let {
                        ContextCompat.startActivity(this, it, null)
                    }
        }
    }
}