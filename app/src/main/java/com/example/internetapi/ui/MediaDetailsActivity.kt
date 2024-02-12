package com.example.internetapi.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.R
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaDetailsBinding
import com.example.internetapi.databinding.AddMediaMeterViewBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.removeRecycleViewItemOnSwipe
import com.example.internetapi.functions.successSnackBar
import com.example.internetapi.functions.toLocalDate
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.MediaDetailsAdapter
import com.example.internetapi.ui.viewModel.MediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.time.LocalDate

@AndroidEntryPoint
class MediaDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_REMOVE_MEDIA_USAGE = "Failed to remove media usage"
    private val FAILED_TO_ADD_MEDIA_USAGE = "Failed to add media usage entry"
    private val FAILED_TO_LOAD_MEDIA_USAGE_DATA = "Failed to load media usage data"
    private val MEDIA_USAGE_REMOVED = "Media usage item removed"

    private val viewModel: MediaViewModel by viewModels()
    private lateinit var binding: ActivityMediaDetailsBinding
    private lateinit var adapter: MediaDetailsAdapter
    private lateinit var meterBinding: AddMediaMeterViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaDetailsBinding.inflate(layoutInflater)
        meterBinding = AddMediaMeterViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MediaDetailsAdapter()
        binding.data.layoutManager = LinearLayoutManager(this)
        binding.data.adapter = adapter

        removeRecycleViewItemOnSwipe(binding.data) { pos -> removeItem(pos) }

        intent.extras?.let { extra ->
            val mediaTypeId = extra.getInt("mediaId")
            extra.getString("name")
            loadData(mediaTypeId)
            binding.fab.setOnClickListener {
                loadDialog(mediaTypeId)
            }
        }
    }

    private fun removeItem(absoluteAdapterPosition: Int) {
        adapter.removeAt(absoluteAdapterPosition)?.let {
            callRemoveItem(it.id)
        }
    }

    private fun callRemoveItem(id: Int) {
        viewModel.removeMediaUsage(id).observe(this) {
            when (it.status) {
                Status.SUCCESS -> successSnackBar(binding.root, MEDIA_USAGE_REMOVED)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_REMOVE_MEDIA_USAGE)
                Status.LOADING -> {}
            }
        }
    }

    private fun loadData(mediaTypeId: Int) {
        viewModel.getMediaUsageByType(mediaTypeId).observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_MEDIA_USAGE_DATA)
                Status.LOADING -> {}
            }
        }
    }

    private fun processSuccess(it: Resource<List<MediaUsage>>) {
        it.data.let { res ->
            res?.let { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun loadDialog(mediaTypeId: Int) {
        meterBinding.root.parent?.let {
            (it as ViewGroup).removeView(meterBinding.root)
        }
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add meter value")
            .setView(meterBinding.root)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val income = meterBinding.value.text.toString()
                when (val v = income.toBigDecimalOrNull()) {
                    null -> Log.w(
                        "AccountDetails",
                        "Income value is not parsable to BigDecimal"
                    )
                    else -> this.addMeterValue(
                        v,
                        meterBinding.date.toLocalDate(),
                        mediaTypeId
                    )
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
        alert.show()
    }

    private fun addMeterValue(value: BigDecimal, date: LocalDate, mediaTypeId: Int) {
        val request = MediaRegisterRequest(mediaTypeId, value, date.year, date.monthValue)
        viewModel.addMediaUsageEntry(request).observe(this) {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_ADD_MEDIA_USAGE)
                Status.LOADING -> {}
            }
        }
    }
}