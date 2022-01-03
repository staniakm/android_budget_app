package com.example.internetapi.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountOutcomeRegisterBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.AccountOutcomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountOutcomeRegisterActivity : AppCompatActivity() {

    private val FAILED_TO_SHOPS: String = "Failed to load shops"
    private val viewModel: AccountOutcomeViewModel by viewModels()
    private lateinit var binding: ActivityAccountOutcomeRegisterBinding
    private val shops: MutableList<Shop> = mutableListOf()
    private val shopItems: MutableList<ShopItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()


        binding.addInvoice.setOnClickListener {
            binding.addInvoice.visibility = View.GONE
            binding.saveInvoice.visibility = View.VISIBLE
            binding.items.visibility = View.VISIBLE
            binding.data1.visibility = View.VISIBLE
            binding.data2.visibility = View.VISIBLE
            binding.fab.visibility = View.VISIBLE
        }

        binding.cancelInvoice.setOnClickListener {
            binding.addInvoice.visibility = View.VISIBLE
            binding.saveInvoice.visibility = View.GONE
            binding.items.visibility = View.GONE
            binding.data1.visibility = View.GONE
            binding.data2.visibility = View.GONE
            binding.fab.visibility = View.GONE
        }

        binding.fab.setOnClickListener {
            addInvoiceItemDialog()
        }
    }

    private fun loadData() {
        viewModel.getShops().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_SHOPS)
                Status.LOADING -> {}
            }
        })
    }

    private fun loadShopItems(shopId: Int) {
        viewModel.getShopItems(shopId).observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadShopItemsOnSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_SHOPS)
                Status.LOADING -> {}
            }
        })
    }

    private fun loadShopItemsOnSuccess(it: Resource<List<ShopItem>>) {
        it.data?.let {
            //todo load shop items
        }
    }

    private fun loadOnSuccess(it: Resource<List<Shop>>) {
        it.data?.let {
            this.shops.addAll(it)
        }
    }

    private fun addInvoiceItemDialog() {
        TODO("Not yet implemented")
    }
}