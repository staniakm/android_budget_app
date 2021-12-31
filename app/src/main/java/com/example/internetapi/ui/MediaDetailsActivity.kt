package com.example.internetapi.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.internetapi.databinding.ActivityMediaBinding
import com.example.internetapi.databinding.ActivityMediaDetailsBinding
import com.google.android.material.snackbar.Snackbar

class MediaDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMediaDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}