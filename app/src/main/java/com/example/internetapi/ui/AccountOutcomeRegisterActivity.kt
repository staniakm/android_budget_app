package com.example.internetapi.ui

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountOutcomeRegisterBinding
import com.example.internetapi.databinding.CreateInvoiceViewBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.toLocalDate
import com.example.internetapi.models.*
import com.example.internetapi.ui.viewModel.AccountOutcomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountOutcomeRegisterActivity : AppCompatActivity() {

    private val FAILED_TO_SHOPS: String = "Failed to load shops"
    private val viewModel: AccountOutcomeViewModel by viewModels()
    private lateinit var binding: ActivityAccountOutcomeRegisterBinding
    private lateinit var invoiceBinding: CreateInvoiceViewBinding
    private val shopItems: MutableList<ShopItem> = mutableListOf()
    private var invoice: Invoice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeRegisterBinding.inflate(layoutInflater)
        invoiceBinding = CreateInvoiceViewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.addInvoice.setOnClickListener {
            intent.extras?.let { extra ->
                invoice = Invoice(extra.getInt("accountId"))


                binding.addInvoice.visibility = View.GONE
                binding.saveInvoice.visibility = View.VISIBLE
                binding.items.visibility = View.VISIBLE
                binding.data1.visibility = View.VISIBLE
                binding.data2.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
                loadData(extra.getString("accountName"))
            }
        }

        binding.cancelInvoice.setOnClickListener {
            hideElements()
        }

        binding.fab.setOnClickListener {
            addInvoiceItemDialog()
        }
    }

    private fun hideElements() {
        binding.addInvoice.visibility = View.VISIBLE
        binding.saveInvoice.visibility = View.GONE
        binding.items.visibility = View.GONE
        binding.data1.visibility = View.GONE
        binding.data2.visibility = View.GONE
        binding.fab.visibility = View.GONE
        invoice = null
    }

    private fun loadData(accountName: String?) {
        viewModel.getShops().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccess(it, accountName)
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

    private fun loadOnSuccess(it: Resource<List<Shop>>, accountName: String?) {
        invoiceBinding.root.parent?.let {
            (it as ViewGroup).removeView(invoiceBinding.root)
        }
        val shopAdapter = ArrayAdapter<Shop>(this, R.layout.simple_spinner_dropdown_item)
        shopAdapter.notifyDataSetChanged()
        it.data?.let { shops ->
            shopAdapter.addAll(shops)
            invoiceBinding.shop.setAdapter(shopAdapter)
            invoiceBinding.shop.setSelection(0)

            invoiceBinding.shop.setOnItemClickListener { parent, view, position, id ->
                Log.i("TAG", "loadOnSuccess: $id, $position, ${parent.getItemAtPosition(position)}")
                invoice?.shop = parent.getItemAtPosition(position) as Shop
            }
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle("Add invoice for")
                .setMessage(accountName)
                .setView(invoiceBinding.root)
                .setPositiveButton("OK") { _, i ->
                    invoice?.apply {
                        this.date = invoiceBinding.date.toLocalDate()
                        this.setShop(invoiceBinding.shop.text.toString())
                    }
                    if (invoice!!.isBasicDataNotFilled()) {
                        errorSnackBar(binding.root, "Fill required data")
                        this.hideElements()
                    }
                    Log.i("TAG", "loadOnSuccess: $invoice")
                    invoiceBinding.shop.text.clear()
                }
                .setNegativeButton("Cancel") { _, _ -> this.hideElements() }
                .setOnCancelListener {
                    this.hideElements()
                }
            alert.show()
        }
    }

    private fun addInvoiceItemDialog() {
        TODO("Not yet implemented")
    }
}