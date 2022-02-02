package com.example.internetapi.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.R
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.MediaAdapter
import com.example.internetapi.ui.adapters.OnItemClickedListener
import com.example.internetapi.ui.viewModel.MediaViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaActivity : AppCompatActivity(), OnItemClickedListener {
    private val viewModel: MediaViewModel by viewModels()
    private lateinit var binding: ActivityMediaBinding
    private lateinit var adapter: MediaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MediaAdapter(this)
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        binding.fab.setOnClickListener {
            createDialog()
        }
        loadData()
    }

    private fun createDialog() {
        val edit = EditText(this)
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add new media type")
            .setView(edit)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                if (edit.text.isNotBlank()) {
                    this.addNewMedia(edit.text.toString().trim())
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alert.show()
    }

    private fun addNewMedia(mediaName: String) {
        viewModel.addNewMediaType(MediaTypeRequest(mediaName)).observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccessAddMedia(it)
                Status.ERROR -> errorSnackBar(
                    binding.root,
                    "Failed to add new media with name: $mediaName"
                )
                Status.LOADING -> {}
            }
        }
    }

    private fun processSuccessAddMedia(it: Resource<MediaType>) {
        it.data.let { res ->
            if (res != null) {
                adapter.addNewMediaType(res)
                Snackbar.make(binding.root, "Dodano ${res.name}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        viewModel.getMediaTypes().observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, "Failed to load media types data")
                Status.LOADING -> Log.println(Log.DEBUG, "MediaType", "Loading.....")
            }
        })
    }

    private fun processSuccess(it: Resource<List<MediaType>>) {
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
            "layout" -> Intent(this, MediaDetailsActivity::class.java).apply {
                this.putExtra("name", item.name)
                this.putExtra("mediaId", item.id)
            }.let {
                ContextCompat.startActivity(this, it, null)
            }
        }
    }
}