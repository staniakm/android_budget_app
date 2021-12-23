package com.example.internetapi.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val preferences = getSharedPreferences("accountSharedPreferences", MODE_PRIVATE)

        binding.serverIp.setText(preferences.getString("hostAddress", ""))

        binding.save.setOnClickListener {
            with(preferences.edit()) {
                putString("hostAddress", binding.serverIp.text.toString())
                commit()
            }
            finish()
        }

    }
}