package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaBinding
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.InvoiceDetailsAdapter
import com.example.internetapi.ui.adapters.MediaAdapter
import com.example.internetapi.ui.viewModel.MediaViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModels()
    private lateinit var binding: ActivityMediaBinding
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MediaAdapter()
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        loadData()
    }

    private fun loadData() {
        viewModel.getMediaTypes().observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    "failed fetched data",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                Status.LOADING -> Log.println(Log.DEBUG, "MediaType", "Loading.....")
            }
        })
    }

    private fun processSuccess(it: Resource<List<MediaType>>) {
        it.data.let { res ->
            if (res != null) {
                if (res.isEmpty()) {
                    Snackbar.make(
                        binding.root,
                        "No data available. Please add new data",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    res.let { list ->
                        adapter.submitList(list)
                    }
                }
            } else {
                Snackbar.make(binding.root, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}