package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.constant.Constant
import com.example.internetapi.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.accounts.visibility = View.INVISIBLE
        binding.budgets.visibility = View.INVISIBLE

        binding.accounts.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        binding.budgets.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.charts.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        binding.media.setOnClickListener {
            startActivity(Intent(this, MediaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = getSharedPreferences("accountSharedPreferences", MODE_PRIVATE)
        preferences.getString("hostAddress", "")?.let {
            if (it.isNotBlank()) setVisibility(View.VISIBLE) else setVisibility(View.INVISIBLE)
            Constant.BASE_URL = it
        }
    }

    private fun setVisibility(visibilityValue: Int) {
        with(binding) {
            accounts.visibility = visibilityValue
            budgets.visibility = visibilityValue
            charts.visibility = visibilityValue
            media.visibility = visibilityValue
        }
    }
}