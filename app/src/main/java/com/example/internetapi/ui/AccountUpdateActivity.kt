package com.example.internetapi.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.internetapi.databinding.ActivityAccountUpdateBinding
import com.example.internetapi.models.Account
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateAccountRequest
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

import android.content.Intent
import com.example.internetapi.models.UpdateAccountResponse


@AndroidEntryPoint
class AccountUpdateActivity : AppCompatActivity() {
    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var binding: ActivityAccountUpdateBinding


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.extras?.let { extra ->
            extra.getSerializable("account")?.let { data ->
                val acc = data as Account
                binding.accName.setText(acc.name)
                binding.accMoney.setText(acc.moneyAmount.toString())
                binding.saveBtn.setOnClickListener {
                    Log.i(
                        "TAG",
                        "Request to update data: ${binding.accName.text} - ${binding.accMoney.text}"
                    )
                    accountViewModel.updateAccount(
                        accountId = acc.id,
                        UpdateAccountRequest(
                            acc.id.toLong(),
                            binding.accName.text.toString(),
                            BigDecimal(binding.accMoney.text.toString())
                        )
                    )
                        .observe(this, {
                            when (it.status) {
                                Status.SUCCESS -> updateAdapter(it.data)
                                Status.ERROR -> Snackbar.make(
                                    binding.root,
                                    "failed update account data",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                                Status.LOADING -> Log.println(
                                    Log.DEBUG,
                                    "AccountDetails",
                                    "Loading....."
                                )
                            }
                        })
                }

            }
        }
    }

    private fun updateAdapter(accId: UpdateAccountResponse?) {
        val returnIntent = Intent()
        accId?.let {
            returnIntent.putExtra("result", it)
            setResult(-1, returnIntent)
        } ?: setResult(RESULT_CANCELED)

        finish()
    }
}