package com.example.internetapi.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.internetapi.databinding.ActivityAccountUpdateBinding
import com.example.internetapi.models.Account
import java.math.BigDecimal

class AccountUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountUpdateBinding


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            extra.getSerializable("account")?.let {
                val acc = it as Account
                binding.accName.setText(acc.name)
                binding.accMoney.setText(acc.moneyAmount.toString())
                binding.saveBtn.setOnClickListener {
                    Log.i("TAG", "Request to update data: ${binding.accName.text} - ${binding.accMoney.text}")
                finish()
                }

            }
        }
    }
}