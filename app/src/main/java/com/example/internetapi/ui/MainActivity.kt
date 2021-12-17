package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accounts.setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        binding.budgets.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

    }
}