package com.example.internetapi.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.view.View
import androidx.activity.viewModels
import com.example.internetapi.databinding.ActivityAccountDetailsBinding
import com.example.internetapi.databinding.ActivityAccountOutcomeRegisterBinding
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class AccountOutcomeRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountOutcomeRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //dummy data
        binding.date.text = LocalDate.now().toString()
        binding.number.text = "1234"
        binding.shop.text = "shop"
        binding.account.text = "account"

        binding.addInvoice.setOnClickListener {
            binding.addInvoice.visibility = View.GONE
            binding.saveInvoice.visibility = View.VISIBLE
            binding.items.visibility = View.VISIBLE
            binding.data1.visibility = View.VISIBLE
            binding.data2.visibility = View.VISIBLE
        }

        binding.cancelInvoice.setOnClickListener {
            binding.addInvoice.visibility = View.VISIBLE
            binding.saveInvoice.visibility = View.GONE
            binding.items.visibility = View.GONE
            binding.data1.visibility = View.GONE
            binding.data2.visibility = View.GONE

        }


    }
}