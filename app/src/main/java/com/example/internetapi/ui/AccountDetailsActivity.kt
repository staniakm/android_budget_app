package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            val name = extra.getString("name", "")
            val income = extra.getString("income", "0.0").toString()
            val outcome = extra.getString("outcome", "0.0").toString()
            binding.name.text = name
            binding.incomeSum.text = income
            binding.outcomeSum.text = outcome

            binding.incomeLay.setOnClickListener {
                Intent(this, AccountIncomeDetails::class.java).apply {
                    this.putExtra("name", name)
                    this.putExtra("accountId", extra.getInt("accountId"))
                    this.putExtra("income", income)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }
            binding.outcomeLay.setOnClickListener {
                Intent(this, AccountOutcomeDetails::class.java).apply {
                    this.putExtra("name", name)
                    this.putExtra("accountId", extra.getInt("accountId"))
                    this.putExtra("outcome", outcome)
                }.let {
                    ContextCompat.startActivity(this, it, null)
                }
            }
        }
    }
}