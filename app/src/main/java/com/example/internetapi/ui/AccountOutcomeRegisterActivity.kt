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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.internetapi.api.Resource
import com.example.internetapi.databinding.ActivityAccountOutcomeRegisterBinding
import com.example.internetapi.databinding.CreateInvoiceItemViewBinding
import com.example.internetapi.databinding.CreateInvoiceViewBinding
import com.example.internetapi.functions.errorSnackBar
import com.example.internetapi.functions.toLocalDate
import com.example.internetapi.models.*
import com.example.internetapi.ui.adapters.InvoiceItemsAdapter
import com.example.internetapi.ui.viewModel.AccountOutcomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.MathContext
import java.math.RoundingMode
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class AccountOutcomeRegisterActivity : AppCompatActivity() {

    private val FAILED_TO_LOAD_SHOPS: String = "Failed to load shops"
    private val FAILED_TO_CREATE_SHOP: String = "Failed to create shop"
    private val viewModel: AccountOutcomeViewModel by viewModels()
    private lateinit var binding: ActivityAccountOutcomeRegisterBinding
    private lateinit var invoiceBinding: CreateInvoiceViewBinding
    private lateinit var invoiceItemBinding: CreateInvoiceItemViewBinding

    private lateinit var adapter: InvoiceItemsAdapter

    private val shopItems: MutableList<ShopItem> = mutableListOf()
    private var invoice: Invoice? = null
    private var currentShopItem: ShopItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountOutcomeRegisterBinding.inflate(layoutInflater)
        invoiceBinding = CreateInvoiceViewBinding.inflate(layoutInflater)
        invoiceItemBinding = CreateInvoiceItemViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = InvoiceItemsAdapter()
        binding.items.adapter = adapter
        binding.items.layoutManager = LinearLayoutManager(this)

        binding.addInvoice.setOnClickListener {
            intent.extras?.let { extra ->
                invoice = Invoice(extra.getInt("accountId"))
                shopItems.clear()

                binding.addInvoice.visibility = View.GONE
                binding.saveInvoice.visibility = View.VISIBLE
                binding.items.visibility = View.VISIBLE
                binding.data1.visibility = View.VISIBLE
                binding.fab.visibility = View.VISIBLE
                loadData(extra.getString("accountName"))
            }
        }

        binding.cancelInvoice.setOnClickListener {
            hideElements()
        }

        binding.saveInvoice.setOnClickListener {
            saveInvoice()
        }

        binding.fab.setOnClickListener {
            addInvoiceItemDialog()
        }
    }

    private fun saveInvoice() {
        invoice?.let { inv ->
            adapter.getItems()
                .map {
                    it.toNewInvoiceItemRequest()
                }.let {
                    NewInvoiceRequest(
                        inv.accountId,
                        inv.shop!!.shopId,
                        inv.date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        it,
                        number = inv.number,
                        description = inv.description
                    )
                }.let {
                    createInvoiceRequest(it)
                }
        }


    }

    private fun hideElements() {
        binding.addInvoice.visibility = View.VISIBLE
        binding.saveInvoice.visibility = View.GONE
        binding.items.visibility = View.GONE
        binding.data1.visibility = View.GONE
        binding.fab.visibility = View.GONE
        invoice = null
        shopItems.clear()
        adapter.clear()
    }

    private fun loadData(accountName: String?) {
        viewModel.getShops().observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccess(it, accountName)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_SHOPS)
                Status.LOADING -> {}
            }
        })
    }

    private fun createInvoiceRequest(newInvoiceRequest: NewInvoiceRequest) {
        viewModel.createNewInvoice(newInvoiceRequest).observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadOnSuccess(it)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_SHOPS)
                Status.LOADING -> {}
            }
        })
    }

    private fun loadOnSuccess(it: Resource<AccountInvoice>) {
        it.data?.let {
            Log.i("TAG", "loadOnSuccess: $it")
        }
    }

    private fun loadShopItems(shop: Shop) {
        if (shop.shopId == -1) {
            viewModel.createShop(shop.name).observe(this, {
                when (it.status) {
                    Status.SUCCESS -> successCreateShop(it)
                    Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_CREATE_SHOP)
                    Status.LOADING -> {}
                }
            })
        } else {
            viewModel.getShopItems(shop.shopId).observe(this, {
                when (it.status) {
                    Status.SUCCESS -> loadShopItemsOnSuccess(it)
                    Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_SHOPS)
                    Status.LOADING -> {}
                }
            })
        }
    }

    private fun successCreateShop(it: Resource<Shop>) {
        it.data?.let {
            invoice?.shop = it
        }
    }


    private fun loadShopItemsOnSuccess(it: Resource<List<ShopItem>>) {
        it.data?.let {
            shopItems.addAll(it)
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
                        this.number = invoiceBinding.number.text.toString()
                    }
                    if (invoice!!.isBasicDataNotFilled()) {
                        errorSnackBar(binding.root, "Fill required data")
                        this.hideElements()
                    } else {
                        this.updateInvoiceData(invoice!!)
                        this.loadShopItems(invoice!!.shop!!)
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

    private fun updateInvoiceData(invoice: Invoice) {
        binding.shop.text = invoice.shop?.name
        binding.date.text = invoice.date.toString()
    }

    private fun addInvoiceItemDialog() {
        invoiceItemBinding.root.parent?.let {
            (it as ViewGroup).removeView(invoiceItemBinding.root)
        }
        currentShopItem = null
        val product = ArrayAdapter<ShopItem>(this, R.layout.simple_spinner_dropdown_item)
        product.notifyDataSetChanged()
        product.addAll(shopItems)
        invoiceItemBinding.product.setAdapter(product)
        invoiceItemBinding.product.setSelection(0)

        invoiceItemBinding.product.setOnItemClickListener { parent, view, position, id ->
            currentShopItem = parent.getItemAtPosition(position) as ShopItem
        }
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("Add product for")
            .setView(invoiceItemBinding.root)
            .setPositiveButton("OK") { _, i ->
                if (currentShopItem == null) {
                    currentShopItem = ShopItem(-1, invoiceItemBinding.product.text.toString())
                }
                with(invoiceItemBinding) {
                    if (price.text.toString().isBlank() || amount.text.toString().isBlank()) {
                        errorSnackBar(binding.root, "Invaliad value for price or amount")
                    } else {
                        InvoiceItem(
                            currentShopItem!!,
                            price.text.toString()
                                .toBigDecimal(MathContext(2, RoundingMode.HALF_UP)),
                            amount.text.toString()
                                .toBigDecimal(MathContext(3, RoundingMode.HALF_UP)),
                            discount.text.toString().ifBlank { "0.0" }
                                .toBigDecimal(MathContext(2, RoundingMode.HALF_UP))
                        ).let {
                            addNewItem(it)
                        }
                    }
                }
                invoiceItemBinding.product.text.clear()
            }
            .setNegativeButton("Cancel") { _, _ -> {} }
        alert.show()
    }

    private fun addNewItem(invoiceItem: InvoiceItem) {
        if (invoiceItem.shopItem.itemId == -1) {
            addItemToShop(invoiceItem)
            return
        }
        return adapter.addItem(invoiceItem)
    }

    private fun addItemToShop(item: InvoiceItem) {

        viewModel.createNewShopItem(invoice!!.shop!!.shopId, item.shopItem.name).observe(this, {
            when (it.status) {
                Status.SUCCESS -> loadShopItemsAdded(it, item)
                Status.ERROR -> errorSnackBar(binding.root, FAILED_TO_LOAD_SHOPS)
                Status.LOADING -> {}
            }
        })
    }

    private fun loadShopItemsAdded(response: Resource<ShopItem>, item: InvoiceItem) {
        response.data?.let {
            adapter.addItem(item.copy(shopItem = it))
        }
    }
}