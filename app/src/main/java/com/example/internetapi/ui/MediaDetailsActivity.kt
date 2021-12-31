package com.example.internetapi.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityMediaDetailsBinding
import com.example.internetapi.databinding.AddMediaMeterViewBinding
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.models.Status
import com.example.internetapi.ui.adapters.MediaDetailsAdapter
import com.example.internetapi.ui.viewModel.MediaViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class MediaDetailsActivity : AppCompatActivity() {
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



        intent.extras?.let { extra ->
            val mediaTypeId = extra.getInt("mediaId")
            val name = extra.getString("name")
            loadData(mediaTypeId)
            binding.fab.setOnClickListener { view ->
                loadDialog(mediaTypeId)
            }
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
                    adapter.submitList(list)
                }
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
            .setPositiveButton("OK") { _, i ->
                val income = meterBinding.value.text.toString()
                when (val v = income.toDoubleOrNull()) {
                    null -> Log.w(
                        "AccountDetails",
                        "Income value is not parsable to BigDecimal"
                    )
                    else -> this.addMeterValue(
                        v,
                        toDate(meterBinding.date),
                        mediaTypeId
                    )
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.i("TAG", "onBindViewHolder: CANCEL")
            }
        alert.show()
    }

    private fun addMeterValue(v: Double, toDate: LocalDate, mediaTypeId: Int) {
        val request = MediaRegisterRequest(mediaTypeId, v, toDate.year, toDate.monthValue)
        viewModel.addMediaUsageEntry(request).observe(this, {
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

    private fun toDate(date: DatePicker): LocalDate {
        return LocalDate.of(date.year, date.month + 1, date.dayOfMonth)
    }
}