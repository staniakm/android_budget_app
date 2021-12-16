package com.example.internetapi.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.internetapi.R
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.databinding.ActivityBudgetUpdateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBudgetUpdateBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            extra.getLong("accountId").let { accountId ->
                Log.i("BudgetUpdateActivity", "onCreate: $accountId")
            }
        }
    }
}