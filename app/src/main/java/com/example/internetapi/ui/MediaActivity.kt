package com.example.internetapi.ui

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaBinding
import com.example.internetapi.models.IncomeType
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
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
            createDialog()
        }
        loadData()
    }

    private fun createDialog() {
        val edit = EditText(this)


        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add new media type")
            .setView(edit)
            .setPositiveButton("OK") { _, _ ->
                if (edit.text.isNotBlank()) {
                    this.addNewMedia(edit.text.toString().trim())
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }
        alert.show()
    }

    private fun addNewMedia(mediaName: String) {
        viewModel.addNewMediaType(MediaTypeRequest(mediaName)).observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccessAddMedia(it)
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    "Failed to add new media with name: $mediaName",
                    Snackbar.LENGTH_SHORT
                )
                    .show()
                Status.LOADING -> Log.println(Log.DEBUG, "MediaType", "Loading.....")
            }
        })
    }

    private fun processSuccessAddMedia(it: Resource<MediaType>) {
        it.data.let { res ->
            if (res != null) {
                adapter.addNewMediaType(res)
            }
        }
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