package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.internetapi.R
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.ui.viewModel.AccountViewModel
import com.example.internetapi.ui.viewModel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountOutcomeDetails : AppCompatActivity() {
    private val invoiceRemoved: String = "Selected invoice removed"
    private val failedToRemoveInvoice: String by lazy { getString(R.string.error_failed_remove_invoice) }
    private val failedToLoadAccountInvoices: String by lazy { getString(R.string.error_failed_load_account_invoices) }

    private val accountViewModel: AccountViewModel by viewModels()
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val name = extras?.getString("name", "") ?: ""
        val outcome = extras?.getString("outcome", "0.0") ?: "0.0"
        val accountId = extras?.getInt("accountId") ?: -1

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    AccountOutcomeDetailsScreen(
                        accountId = accountId,
                        name = name,
                        outcome = outcome,
                        accountViewModel = accountViewModel,
                        invoiceViewModel = invoiceViewModel,
                        invoiceRemoved = invoiceRemoved,
                        failedToRemoveInvoice = failedToRemoveInvoice,
                        failedToLoadAccountInvoices = failedToLoadAccountInvoices,
                        onOpenInvoiceDetails = { invoiceId ->
                            Intent(this, InvoiceDetailsActivity::class.java).apply {
                                putExtra("invoiceId", invoiceId)
                            }.also { startActivity(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountOutcomeDetailsScreen(
    accountId: Int,
    name: String,
    outcome: String,
    accountViewModel: AccountViewModel,
    invoiceViewModel: InvoiceViewModel,
    invoiceRemoved: String,
    failedToRemoveInvoice: String,
    failedToLoadAccountInvoices: String,
    onOpenInvoiceDetails: (Long) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val missingAccountIdMessage = stringResource(id = R.string.error_missing_account_id)
    val successInvoiceUpdatedMessage = stringResource(id = R.string.success_invoice_account_updated)
    val failedUpdateInvoiceAccountMessage = stringResource(id = R.string.error_failed_update_invoice_account)

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    val invoicesLiveData = remember(accountId) {
        if (accountId <= 0) null else accountViewModel.accountInvoices(accountId)
    }
    val invoicesResource = observeResource(invoicesLiveData)

    var invoices by remember { mutableStateOf<List<AccountInvoice>>(emptyList()) }
    LaunchedEffect(invoicesResource?.data) {
        invoicesResource?.data?.let { invoices = it }
    }

    LaunchedEffect(invoicesResource?.status, accountId) {
        if (accountId <= 0) {
            showMessage(missingAccountIdMessage)
        }
        if (invoicesResource?.status == Status.ERROR) {
            showMessage(failedToLoadAccountInvoices)
        }
    }

    var pendingDelete by remember { mutableStateOf<AccountInvoice?>(null) }
    var deleteLiveData by remember { mutableStateOf<androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<Void>>?>(null) }
    val deleteResource = observeResource(deleteLiveData)

    var updateAccountLiveData by remember {
        mutableStateOf<androidx.lifecycle.LiveData<com.example.internetapi.api.Resource<AccountInvoice>>?>(null)
    }
    val updateAccountResource = observeResource(updateAccountLiveData)
    var pendingAccountChange by remember { mutableStateOf<Pair<Long, Int>?>(null) }

    LaunchedEffect(deleteResource?.status) {
        when (deleteResource?.status) {
            Status.SUCCESS -> {
                pendingDelete?.let { invoice ->
                    invoices = invoices.filter { it.listId != invoice.listId }
                }
                showMessage(invoiceRemoved)
                pendingDelete = null
                deleteLiveData = null
            }
            Status.ERROR -> {
                showMessage(failedToRemoveInvoice)
                pendingDelete = null
                deleteLiveData = null
            }
            else -> Unit
        }
    }

    LaunchedEffect(updateAccountResource?.status) {
        when (updateAccountResource?.status) {
            Status.SUCCESS -> {
                val pending = pendingAccountChange
                if (pending != null && pending.second != accountId) {
                    invoices = invoices.filter { it.listId != pending.first }
                }
                showMessage(successInvoiceUpdatedMessage)
                pendingAccountChange = null
                updateAccountLiveData = null
            }
            Status.ERROR -> {
                showMessage(failedUpdateInvoiceAccountMessage)
                pendingAccountChange = null
                updateAccountLiveData = null
            }
            else -> Unit
        }
    }

    var accountToChange by remember { mutableStateOf<AccountInvoice?>(null) }
    val isLoading = invoicesResource?.status == Status.LOADING ||
        deleteResource?.status == Status.LOADING ||
        updateAccountResource?.status == Status.LOADING

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
                        text = name,
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WYDATKI:",
                                modifier = Modifier.weight(0.35f),
                                style = MaterialTheme.typography.h6
                            )
                            Text(
                                text = outcome,
                                modifier = Modifier.weight(0.65f),
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }

                items(invoices, key = { it.listId }) { invoice ->
                    AccountInvoiceCard(
                        invoice = invoice,
                        onClick = { onOpenInvoiceDetails(invoice.listId) },
                        onChangeAccount = { accountToChange = invoice },
                        onDelete = { pendingDelete = invoice }
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

    if (pendingDelete != null) {
        val invoice = pendingDelete
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(id = R.string.dialog_title_invoice_removal)) },
            text = { Text(text = stringResource(id = R.string.dialog_message_remove_selected_invoice)) },
            confirmButton = {
                TextButton(onClick = {
                    if (invoice != null) {
                        deleteLiveData = invoiceViewModel.deleteInvoice(invoice.listId)
                    }
                }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (accountToChange != null) {
        val item = accountToChange
        var selected by remember(item, accountId) {
            mutableStateOf(AccountHolder.accounts.firstOrNull { it.id == accountId })
        }
        var expanded by remember(item) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { accountToChange = null },
            title = { Text(text = stringResource(id = R.string.dialog_title_change_account_invoice)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = item?.listId?.toString().orEmpty())
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            elevation = 2.dp
                        ) {
                            Text(
                                text = selected?.name.orEmpty(),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.body1
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            AccountHolder.accounts.forEach { account ->
                                DropdownMenuItem(onClick = {
                                    selected = account
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
                TextButton(onClick = {
                    val invoice = item
                    val selectedAccount = selected
                    if (invoice != null && selectedAccount != null) {
                        pendingAccountChange = invoice.listId to selectedAccount.id
                        updateAccountLiveData = invoiceViewModel.updateInvoiceAccount(
                            UpdateInvoiceAccountRequest(invoice.listId, accountId, selectedAccount.id)
                        )
                    }
                    accountToChange = null
                }) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToChange = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountInvoiceCard(
    invoice: AccountInvoice,
    onClick: () -> Unit,
    onChangeAccount: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onChangeAccount),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
                }
                Text(
                    text = invoice.name,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = onChangeAccount,
                    modifier = Modifier.padding(start = 4.dp, end = 0.dp)
                ) {
                    Text(
                        text = stringResource(id = com.example.internetapi.R.string.change_account),
                        textAlign = TextAlign.End
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.date,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(0.55f)
                )
                Text(
                    text = MoneyFormatter.df.format(invoice.price),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(0.45f)
                )
            }
        }
    }
}
