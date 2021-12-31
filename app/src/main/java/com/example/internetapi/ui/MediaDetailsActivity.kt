package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaDetailsBinding
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.MediaDetailsAdapter
import com.example.internetapi.ui.viewModel.MediaViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaDetailsActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModels()
    private lateinit var binding: ActivityMediaDetailsBinding
    private lateinit var adapter: MediaDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MediaDetailsAdapter()
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        intent.extras?.let { extra ->
            val mediaTypeId = extra.getInt("mediaId")
            val name = extra.getString("name")
            loadData(mediaTypeId)
        }
    }

    private fun loadData(mediaTypeId: Int) {
        viewModel.getMediaUsageByType(mediaTypeId).observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    "Failed to fetched media usage data",
                    Snackbar.LENGTH_LONG
                ).show()
                Status.LOADING -> Log.println(Log.DEBUG, "MediaType", "Loading.....")
            }
        })
    }

    private fun processSuccess(it: Resource<List<MediaUsage>>) {
        it.data.let { res ->
            if (res != null) {
                res.let { list ->
                    Log.i("TAG", "processSuccess: load data here")
                    list.sortedByDescending { it.year }.sortedByDescending { it.month }
                        .let {
                            adapter.submitList(it)
                        }
                }
            }
        }
    }
}