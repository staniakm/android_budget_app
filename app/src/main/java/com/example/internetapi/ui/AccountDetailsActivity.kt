package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.internetapi.R
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.DateFormatter.yyyymm
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.AccountOperation
import com.example.internetapi.models.IncomeType
import com.example.internetapi.models.Status
import com.example.internetapi.models.TransferMoneyRequest
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AccountDetailsActivity : AppCompatActivity() {

    private val tag: String = "AccountDetailsActivity"
    private val failedToGetIncomeType = "Failed to load income type"
    private val failedToLoadOperations = "Failed to load account operations"

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val name = extras?.getString("name", "") ?: ""
        val income = extras?.getString("income", "0.0").toString()
        val outcome = extras?.getString("outcome", "0.0").toString()
        val accountId = extras?.getInt("accountId") ?: -1

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    AccountDetailsScreen(
                        viewModel = accountViewModel,
                        accountId = accountId,
                        accountName = name,
                        incomeSum = income,
                        outcomeSum = outcome,
                        failedIncomeTypeMessage = failedToGetIncomeType,
                        failedOperationsMessage = failedToLoadOperations,
                        onOpenIncomeDetails = {
                            Intent(this, AccountIncomeDetails::class.java).apply {
                                putExtra("name", name)
                                putExtra("accountId", accountId)
                                putExtra("income", income)
                            }.also { startActivity(it) }
                        },
                        onOpenOutcomeDetails = {
                            Intent(this, AccountOutcomeDetails::class.java).apply {
                                putExtra("name", name)
                                putExtra("accountId", accountId)
                                putExtra("outcome", outcome)
                            }.also { startActivity(it) }
                        },
                        onOpenOutcomeRegister = {
                            Intent(this, AccountOutcomeRegisterActivity::class.java).apply {
                                putExtra("accountId", accountId)
                                putExtra("accountName", name)
                            }.also { startActivity(it) }
                        },
                        onOpenInvoiceDetails = { invoiceId ->
                            Intent(this, InvoiceDetailsActivity::class.java).apply {
                                putExtra("invoiceId", invoiceId)
                            }.also { startActivity(it) }
                        },
                        logTag = tag,
                        onTransferMoney = { value, targetAccount ->
                            transferMoney(accountId, value, targetAccount)
                        },
                        onAddIncome = { value, date, description ->
                            addIncome(accountId, value, date, description)
                        }
                    )
                }
            }
        }
    }

    private fun transferMoney(
        accountId: Int,
        value: BigDecimal,
        targetAccount: Int,
    ): androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<UpdateAccountResponse>>? {
        if (value > BigDecimal.ZERO && accountId != targetAccount) {
            return accountViewModel.transferMoney(TransferMoneyRequest(accountId, value, targetAccount))
        }
        return null
    }

    private fun addIncome(
        accountId: Int,
        value: BigDecimal,
        date: LocalDate,
        description: String,
    ): androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<List<AccountIncome>>>? {
        if (value > BigDecimal.ZERO) {
            return accountViewModel.addIncome(
                AccountIncomeRequest(
                    accountId,
                    value,
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    description
                )
            )
        }
        return null
    }
}

@Composable
private fun AccountDetailsScreen(
    viewModel: AccountViewModel,
    accountId: Int,
    accountName: String,
    incomeSum: String,
    outcomeSum: String,
    failedIncomeTypeMessage: String,
    failedOperationsMessage: String,
    onOpenIncomeDetails: () -> Unit,
    onOpenOutcomeDetails: () -> Unit,
    onOpenOutcomeRegister: () -> Unit,
    onOpenInvoiceDetails: (Long) -> Unit,
    logTag: String,
    onTransferMoney: (BigDecimal, Int) -> androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<UpdateAccountResponse>>?,
    onAddIncome: (BigDecimal, LocalDate, String) -> androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<List<AccountIncome>>>?,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    var refreshKey by rememberSaveable(accountId) { mutableStateOf(0) }
    DisposableEffect(lifecycleOwner, accountId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && accountId > 0) {
                refreshKey += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val operationsLiveData = remember(accountId, refreshKey) {
        if (accountId <= 0) null else viewModel.getOperations(accountId)
    }
    val operationsResource = observeResource(operationsLiveData)

    var showIncomeDialog by rememberSaveable(accountId) { mutableStateOf(false) }
    var incomeTypesKey by rememberSaveable(accountId) { mutableStateOf(0) }
    val incomeTypesLiveData = remember(incomeTypesKey) {
        if (incomeTypesKey == 0) null else viewModel.getIncomeTypes()
    }
    val incomeTypesResource = observeResource(incomeTypesLiveData)

    var showTransferDialog by rememberSaveable(accountId) { mutableStateOf(false) }

    var transferRequestKey by rememberSaveable(accountId) { mutableStateOf(0) }
    var transferLiveData by remember {
        mutableStateOf<androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<UpdateAccountResponse>>?>(null)
    }
    val transferResource = observeResource(transferLiveData)

    var addIncomeRequestKey by rememberSaveable(accountId) { mutableStateOf(0) }
    var addIncomeLiveData by remember {
        mutableStateOf<androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<List<AccountIncome>>>?>(null)
    }
    val addIncomeResource = observeResource(addIncomeLiveData)

    LaunchedEffect(operationsResource?.status, accountId) {
        if (accountId <= 0) {
            showMessage("Missing accountId")
        }
        if (operationsResource?.status == Status.ERROR) {
            showMessage(failedOperationsMessage)
        }
    }

    LaunchedEffect(incomeTypesResource?.status) {
        if (incomeTypesResource?.status == Status.ERROR) {
            showMessage(failedIncomeTypeMessage)
        }
    }

    LaunchedEffect(transferResource?.status, transferRequestKey) {
        when (transferResource?.status) {
            Status.SUCCESS -> {
                showMessage("Money transfer completed")
                refreshKey += 1
                transferLiveData = null
            }
            Status.ERROR -> {
                showMessage("Failed to transfer money")
                transferLiveData = null
            }
            else -> Unit
        }
    }

    LaunchedEffect(addIncomeResource?.status, addIncomeRequestKey) {
        when (addIncomeResource?.status) {
            Status.SUCCESS -> {
                showMessage("Income added")
                refreshKey += 1
                addIncomeLiveData = null
            }
            Status.ERROR -> {
                showMessage("Failed to add income")
                addIncomeLiveData = null
            }
            else -> Unit
        }
    }

    val accountMonth = remember(accountName) {
        "$accountName - ${LocalDate.now().plusMonths(MonthSelector.month.toLong()).format(yyyymm)}"
    }
    val operations = operationsResource?.data ?: emptyList()
    val isLoading = operationsResource?.status == Status.LOADING || incomeTypesResource?.status == Status.LOADING

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = accountMonth,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SumRow(
                                label = stringResource(id = R.string.income),
                                value = incomeSum,
                                onClick = onOpenIncomeDetails
                            )
                            SumRow(
                                label = stringResource(id = R.string.outcome),
                                value = outcomeSum,
                                onClick = onOpenOutcomeDetails
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showIncomeDialog = true
                                incomeTypesKey += 1
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.add_income))
                        }
                        Button(
                            onClick = { showTransferDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(id = R.string.transfer_money))
                        }
                    }
                }

                item {
                    Button(onClick = onOpenOutcomeRegister, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(id = R.string.add_invoice))
                    }
                }

                items(operations, key = { "${it.id}-${it.type}" }) { operation ->
                    AccountOperationCard(
                        operation = operation,
                        onOpenInvoiceDetails = onOpenInvoiceDetails,
                        onIncomeClick = {
                            Log.i(logTag, "onClick: Not implemented")
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showIncomeDialog) {
        val descriptions = incomeTypesResource?.data ?: emptyList()
        var valueText by rememberSaveable { mutableStateOf("0") }
        var selectedYear by rememberSaveable { mutableStateOf(LocalDate.now().year) }
        var selectedMonth by rememberSaveable { mutableStateOf(LocalDate.now().monthValue) }
        var selectedDay by rememberSaveable { mutableStateOf(LocalDate.now().dayOfMonth) }
        var selectedDescription by rememberSaveable(descriptions) {
            mutableStateOf(descriptions.firstOrNull()?.name ?: "")
        }
        var expanded by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = { Text(text = "Add account income") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = accountName, style = MaterialTheme.typography.body1)
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
                            value = selectedDescription,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            label = { Text("Description") },
                            readOnly = true,
                            singleLine = true
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            descriptions.forEach { incomeType ->
                                DropdownMenuItem(onClick = {
                                    selectedDescription = incomeType.name
                                    expanded = false
                                }) {
                                    Text(text = incomeType.name)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Value") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val raw = valueText.trim()
                        val parsedValue = raw.replace(',', '.').toBigDecimalOrNull()
                        if (parsedValue == null) {
                            Log.w(logTag, "Income value is not parsable to BigDecimal")
                            showMessage("Provided value: $raw - is not parsable to number")
                            return@TextButton
                        }

                        addIncomeLiveData = onAddIncome(
                            parsedValue,
                            LocalDate.of(selectedYear, selectedMonth, selectedDay),
                            selectedDescription
                        )
                        addIncomeRequestKey += 1
                        showIncomeDialog = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (showTransferDialog) {
        var valueText by rememberSaveable { mutableStateOf("0") }
        var selectedTarget by remember { mutableStateOf(AccountHolder.accounts.firstOrNull()) }
        var expanded by rememberSaveable { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(text = "Transfer money") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = accountName, style = MaterialTheme.typography.body1)
                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Value") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedTarget?.name ?: "",
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            label = { Text("Target account") },
                            readOnly = true,
                            singleLine = true
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            AccountHolder.accounts.forEach { account ->
                                DropdownMenuItem(onClick = {
                                    selectedTarget = account
                                    expanded = false
                                }) {
                                    Text(text = account.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val raw = valueText.trim()
                        val target = selectedTarget
                        val parsedValue = raw.replace(',', '.').toBigDecimalOrNull()
                        if (parsedValue == null) {
                            showMessage("Provided value: $raw - is not parsable to number")
                            return@TextButton
                        }
                        if (target == null) {
                            showMessage("No target account selected")
                            return@TextButton
                        }
                        transferLiveData = onTransferMoney(parsedValue, target.id)
                        transferRequestKey += 1
                        showTransferDialog = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SumRow(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(0.35f), style = MaterialTheme.typography.h6)
        Text(text = value, modifier = Modifier.weight(0.65f), style = MaterialTheme.typography.h6)
    }
}

@Composable
private fun AccountOperationCard(
    operation: AccountOperation,
    onOpenInvoiceDetails: (Long) -> Unit,
    onIncomeClick: () -> Unit,
) {
    val isOutcome = operation.type == "OUTCOME"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (isOutcome) {
                    onOpenInvoiceDetails(operation.id)
                } else {
                    onIncomeClick()
                }
            },
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = operation.date,
                modifier = Modifier.weight(0.45f),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = MoneyFormatter.df.format(operation.value),
                modifier = Modifier.weight(0.4f),
                style = MaterialTheme.typography.body1
            )
            Image(
                painter = painterResource(id = if (isOutcome) R.drawable.outcome else R.drawable.income),
                contentDescription = operation.type,
                modifier = Modifier.weight(0.15f)
            )
        }
    }
}
