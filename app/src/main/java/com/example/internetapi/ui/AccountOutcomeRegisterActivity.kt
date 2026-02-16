package com.example.internetapi.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.internetapi.R
import com.example.internetapi.config.AmountFormatter
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.CreateInvoiceResponse
import com.example.internetapi.models.Invoice
import com.example.internetapi.models.InvoiceItem
import com.example.internetapi.models.NewInvoiceRequest
import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.AccountOutcomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AccountOutcomeRegisterActivity : AppCompatActivity() {

    private val failedToLoadShops: String = "Failed to load shops"
    private val failedToCreateShop: String = "Failed to create shop"
    private val viewModel: AccountOutcomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val extras = intent.extras
        val accountId = extras?.getInt("accountId") ?: -1
        val accountName = extras?.getString("accountName")

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    AccountOutcomeRegisterScreen(
                        viewModel = viewModel,
                        accountId = accountId,
                        accountName = accountName,
                        failedToLoadShops = failedToLoadShops,
                        failedToCreateShop = failedToCreateShop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun AccountOutcomeRegisterScreen(
    viewModel: AccountOutcomeViewModel,
    accountId: Int,
    accountName: String?,
    failedToLoadShops: String,
    failedToCreateShop: String,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    var invoice by rememberSaveable { mutableStateOf<Invoice?>(null) }
    var invoiceItems by remember { mutableStateOf<List<InvoiceItem>>(emptyList()) }
    var shopItems by remember { mutableStateOf<List<ShopItem>>(emptyList()) }

    var showCreateInvoiceDialog by rememberSaveable { mutableStateOf(false) }
    var showAddItemDialog by rememberSaveable { mutableStateOf(false) }

    var shopsKey by rememberSaveable { mutableStateOf(0) }
    val shopsLiveData = remember(shopsKey) {
        if (shopsKey == 0) null else viewModel.getShops()
    }
    val shopsResource = observeResource(shopsLiveData)

    var createShopKey by rememberSaveable { mutableStateOf(0) }
    var createShopName by remember { mutableStateOf<String?>(null) }
    val createShopLiveData = remember(createShopKey) {
        val name = createShopName
        if (createShopKey == 0 || name.isNullOrBlank()) null else viewModel.createShop(name)
    }
    val createShopResource = observeResource(createShopLiveData)

    var shopItemsKey by rememberSaveable { mutableStateOf(0) }
    var selectedShopIdForItems by remember { mutableStateOf<Int?>(null) }
    val shopItemsLiveData = remember(shopItemsKey) {
        val shopId = selectedShopIdForItems
        if (shopItemsKey == 0 || shopId == null) null else viewModel.getShopItems(shopId)
    }
    val shopItemsResource = observeResource(shopItemsLiveData)

    var createShopItemKey by rememberSaveable { mutableStateOf(0) }
    var createShopItemName by remember { mutableStateOf<String?>(null) }
    var pendingInvoiceItem by remember { mutableStateOf<InvoiceItem?>(null) }
    val createShopItemLiveData = remember(createShopItemKey) {
        val shopId = invoice?.shop?.shopId
        val name = createShopItemName
        if (createShopItemKey == 0 || shopId == null || name.isNullOrBlank()) {
            null
        } else {
            viewModel.createNewShopItem(shopId, name)
        }
    }
    val createShopItemResource = observeResource(createShopItemLiveData)

    var createInvoiceKey by rememberSaveable { mutableStateOf(0) }
    var invoiceRequest by remember { mutableStateOf<NewInvoiceRequest?>(null) }
    val createInvoiceLiveData = remember(createInvoiceKey) {
        val request = invoiceRequest
        if (createInvoiceKey == 0 || request == null) null else viewModel.createNewInvoice(request)
    }
    val createInvoiceResource = observeResource(createInvoiceLiveData)

    LaunchedEffect(Unit) {
        showCreateInvoiceDialog = true
        shopsKey += 1
    }

    LaunchedEffect(shopsResource?.status) {
        if (shopsResource?.status == Status.ERROR) {
            showMessage(failedToLoadShops)
        }
    }

    LaunchedEffect(shopItemsResource?.status, shopItemsResource?.data) {
        when (shopItemsResource?.status) {
            Status.SUCCESS -> shopItems = shopItemsResource.data ?: emptyList()
            Status.ERROR -> showMessage(failedToLoadShops)
            else -> Unit
        }
    }

    LaunchedEffect(createShopResource?.status, createShopResource?.data) {
        when (createShopResource?.status) {
            Status.SUCCESS -> {
                val shop = createShopResource.data
                if (shop != null) {
                    invoice?.shop = shop
                }
                createShopName = null
            }
            Status.ERROR -> {
                showMessage(failedToCreateShop)
                createShopName = null
            }
            else -> Unit
        }
    }

    LaunchedEffect(createShopItemResource?.status, createShopItemResource?.data) {
        when (createShopItemResource?.status) {
            Status.SUCCESS -> {
                val created = createShopItemResource.data
                val pending = pendingInvoiceItem
                if (created != null && pending != null) {
                    invoiceItems = invoiceItems + pending.copy(shopItem = created)
                    if (shopItems.none { it.itemId == created.itemId }) {
                        shopItems = shopItems + created
                    }
                }
                pendingInvoiceItem = null
                createShopItemName = null
            }
            Status.ERROR -> {
                showMessage(failedToLoadShops)
                pendingInvoiceItem = null
                createShopItemName = null
            }
            else -> Unit
        }
    }

    LaunchedEffect(createInvoiceResource?.status, createInvoiceResource?.data) {
        when (createInvoiceResource?.status) {
            Status.SUCCESS -> {
                val data = createInvoiceResource.data
                if (data != null) {
                    hideElements(
                        onUpdateInvoice = { invoice = it },
                        onUpdateInvoiceItems = { invoiceItems = it },
                        onUpdateShopItems = { shopItems = it },
                        onUpdateShowAddItemDialog = { showAddItemDialog = it }
                    )
                    showMessage("New invoice created for total sum: ${data.sum}")
                }
            }
            Status.ERROR -> showMessage(failedToLoadShops)
            else -> Unit
        }
    }

    val isLoading = shopsResource?.status == Status.LOADING ||
        createShopResource?.status == Status.LOADING ||
        shopItemsResource?.status == Status.LOADING ||
        createShopItemResource?.status == Status.LOADING ||
        createInvoiceResource?.status == Status.LOADING

    val total = remember(invoiceItems) {
        invoiceItems.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalPrice()) }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            if (invoice != null) {
                FloatingActionButton(onClick = { showAddItemDialog = true }) {
                    Text(text = "+")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        hideElements(
                            onUpdateInvoice = { invoice = it },
                            onUpdateInvoiceItems = { invoiceItems = it },
                            onUpdateShopItems = { shopItems = it },
                            onUpdateShowAddItemDialog = { showAddItemDialog = it }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.cancel_outcome))
                }

                if (invoice == null) {
                    Button(
                        onClick = {
                            showCreateInvoiceDialog = true
                            shopsKey += 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.create_outcome))
                    }
                } else {
                    HeaderRow(
                        date = invoice?.date.toString(),
                        shop = invoice?.shop?.name.orEmpty(),
                        total = MoneyFormatter.df.format(total)
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(invoiceItems, key = { _, item -> item.timestamp }) { index, item ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = { state ->
                                    if (state == DismissValue.DismissedToStart) {
                                        invoiceItems = invoiceItems.filterIndexed { i, _ -> i != index }
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                background = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(text = "Delete", color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissContent = {
                                    InvoiceItemCard(item = item)
                                }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (invoiceItems.isEmpty()) {
                                showMessage("Empty invoice item list.\n Unable to save invoice")
                                return@Button
                            }

                            val currentInvoice = invoice
                            if (currentInvoice?.shop == null || currentInvoice.date == null) {
                                showMessage("Fill required data")
                                return@Button
                            }

                            invoiceRequest = NewInvoiceRequest(
                                currentInvoice.accountId,
                                currentInvoice.shop!!.shopId,
                                currentInvoice.date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                invoiceItems.map { it.toNewInvoiceItemRequest() },
                                number = currentInvoice.number,
                                description = currentInvoice.description
                            )
                            createInvoiceKey += 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.save_outcome))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showCreateInvoiceDialog) {
        val shops = shopsResource?.data ?: emptyList()
        var selectedYear by rememberSaveable { mutableStateOf(LocalDate.now().year) }
        var selectedMonth by rememberSaveable { mutableStateOf(LocalDate.now().monthValue) }
        var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().dayOfMonth) }
        var shopText by rememberSaveable { mutableStateOf("") }
        var invoiceNumber by rememberSaveable { mutableStateOf("") }
        var selectedShop by remember { mutableStateOf<Shop?>(null) }
        var expanded by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showCreateInvoiceDialog = false
                hideElements(
                    onUpdateInvoice = { invoice = it },
                    onUpdateInvoiceItems = { invoiceItems = it },
                    onUpdateShopItems = { shopItems = it },
                    onUpdateShowAddItemDialog = { showAddItemDialog = it }
                )
            },
            title = { Text(text = "Add invoice for") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = accountName.orEmpty())
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            DatePicker(context).apply {
                                init(selectedYear, selectedMonth - 1, selectedDay) { _, y, m, d ->
                                    selectedYear = y
                                    selectedMonth = m + 1
                                    selectedDay = d
                                }
                            }
                        },
                        update = { picker ->
                            if (picker.year != selectedYear || picker.month != selectedMonth - 1 || picker.dayOfMonth != selectedDay) {
                                picker.updateDate(selectedYear, selectedMonth - 1, selectedDay)
                            }
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = shopText,
                            onValueChange = {
                                shopText = it
                                selectedShop = shops.firstOrNull { s -> s.name.equals(it, ignoreCase = true) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            label = { Text(stringResource(id = R.string.shop)) },
                            singleLine = true
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            shops.forEach { shop ->
                                DropdownMenuItem(onClick = {
                                    selectedShop = shop
                                    shopText = shop.name
                                    expanded = false
                                }) {
                                    Text(text = shop.name)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(id = R.string.invoice_number)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newInvoice = Invoice(accountId)
                    newInvoice.date = LocalDate.of(selectedYear, selectedMonth, selectedDay)
                    newInvoice.shop = selectedShop
                    newInvoice.setShop(shopText)
                    newInvoice.number = invoiceNumber

                    if (newInvoice.isBasicDataNotFilled()) {
                        showMessage("Fill required data")
                        hideElements(
                            onUpdateInvoice = { invoice = it },
                            onUpdateInvoiceItems = { invoiceItems = it },
                            onUpdateShopItems = { shopItems = it },
                            onUpdateShowAddItemDialog = { showAddItemDialog = it }
                        )
                    } else {
                        invoice = newInvoice
                        invoiceItems = emptyList()
                        shopItems = emptyList()
                        val shop = newInvoice.shop
                        if (shop != null) {
                            if (shop.shopId == -1) {
                                createShopName = shop.name
                                createShopKey += 1
                            } else {
                                selectedShopIdForItems = shop.shopId
                                shopItemsKey += 1
                            }
                        }
                    }

                    showCreateInvoiceDialog = false
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateInvoiceDialog = false
                    hideElements(
                        onUpdateInvoice = { invoice = it },
                        onUpdateInvoiceItems = { invoiceItems = it },
                        onUpdateShopItems = { shopItems = it },
                        onUpdateShowAddItemDialog = { showAddItemDialog = it }
                    )
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (showAddItemDialog) {
        val invalidPriceOrAmountMessage = stringResource(id = R.string.error_invalid_price_or_amount)
        var productText by rememberSaveable { mutableStateOf("") }
        var selectedItem by remember { mutableStateOf<ShopItem?>(null) }
        var priceText by rememberSaveable { mutableStateOf("") }
        var amountText by rememberSaveable { mutableStateOf("") }
        var discountText by rememberSaveable { mutableStateOf("") }
        var expanded by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text(text = "Add product for") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = productText,
                            onValueChange = {
                                productText = it
                                selectedItem = shopItems.firstOrNull { s -> s.name.equals(it, ignoreCase = true) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            label = { Text(stringResource(id = R.string.product)) },
                            singleLine = true
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            shopItems.forEach { item ->
                                DropdownMenuItem(onClick = {
                                    selectedItem = item
                                    productText = item.name
                                    expanded = false
                                }) {
                                    Text(text = item.name)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(id = R.string.price)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(id = R.string.amount)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = discountText,
                            onValueChange = { discountText = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(id = R.string.discount)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (priceText.isBlank() || amountText.isBlank()) {
                        showMessage(invalidPriceOrAmountMessage)
                        return@TextButton
                    }

                    val price = priceText.trim().replace(',', '.').toBigDecimalOrNull()
                    val amount = amountText.trim().replace(',', '.').toBigDecimalOrNull()
                    val discount = discountText.trim().ifBlank { "0.0" }.replace(',', '.').toBigDecimalOrNull()

                    if (price == null || amount == null || discount == null) {
                        showMessage(invalidPriceOrAmountMessage)
                        return@TextButton
                    }

                    val current = selectedItem ?: ShopItem(-1, productText)
                    val item = InvoiceItem(current, price, amount, discount)

                    if (item.shopItem.itemId == -1) {
                        val shopId = invoice?.shop?.shopId
                        if (shopId == null || shopId <= 0) {
                            showMessage(failedToLoadShops)
                            return@TextButton
                        }
                        pendingInvoiceItem = item
                        createShopItemName = item.shopItem.name
                        createShopItemKey += 1
                    } else {
                        invoiceItems = invoiceItems + item
                    }

                    showAddItemDialog = false
                }) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

private fun hideElements(
    onUpdateInvoice: (Invoice?) -> Unit,
    onUpdateInvoiceItems: (List<InvoiceItem>) -> Unit,
    onUpdateShopItems: (List<ShopItem>) -> Unit,
    onUpdateShowAddItemDialog: (Boolean) -> Unit,
) {
    onUpdateInvoice(null)
    onUpdateInvoiceItems(emptyList())
    onUpdateShopItems(emptyList())
    onUpdateShowAddItemDialog(false)
}

@Composable
private fun HeaderRow(
    date: String,
    shop: String,
    total: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = date, modifier = Modifier.weight(0.3f), style = MaterialTheme.typography.body1)
        Text(text = shop, modifier = Modifier.weight(0.45f), style = MaterialTheme.typography.body1)
        Text(text = total, modifier = Modifier.weight(0.25f), style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InvoiceItemCard(item: InvoiceItem) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.shopItem.name,
                    modifier = Modifier.weight(0.65f),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Suma: ${MoneyFormatter.df.format(item.totalPrice())}",
                    modifier = Modifier.weight(0.35f),
                    style = MaterialTheme.typography.body2
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ilość: ${AmountFormatter.df.format(item.amount)}",
                    modifier = Modifier.weight(0.3f),
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Cena: ${MoneyFormatter.df.format(item.price)}",
                    modifier = Modifier.weight(0.35f),
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Rabat: ${MoneyFormatter.df.format(item.discount)}",
                    modifier = Modifier.weight(0.35f),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
