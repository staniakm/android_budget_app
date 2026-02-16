package com.example.internetapi.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.internetapi.config.AccountHolder
import com.example.internetapi.config.DateFormatter
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.functions.getSerializableCompat
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Account
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.ui.viewModel.AccountViewModel
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate


@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AccountScreen(
                        viewModel = accountViewModel,
                        onOpenDetails = { item ->
                            Intent(this, AccountDetailsActivity::class.java).apply {
                                putExtra("name", item.name)
                                putExtra("accountId", item.id)
                                putExtra("income", MoneyFormatter.df.format(item.income))
                                putExtra("outcome", MoneyFormatter.df.format(item.expense))
                            }.let { startActivity(it) }
                        },
                        onOpenEdit = { item, launcher ->
                            Intent(this, AccountUpdateActivity::class.java).apply {
                                putExtra("account", item)
                            }.let { launcher.launch(it) }
                        }
                    )
                }
            }
        }
    }
}

private object AccountDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 16
    const val CardInnerSpacing = 10
}

@Composable
private fun AccountScreen(
    viewModel: AccountViewModel,
    onOpenDetails: (Account) -> Unit,
    onOpenEdit: (Account, androidx.activity.result.ActivityResultLauncher<Intent>) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var refreshKey by rememberSaveable { mutableStateOf(0) }
    var overrides by remember { mutableStateOf<Map<Int, UpdateAccountResponse>>(emptyMap()) }

    val accountsLiveData = remember(refreshKey) { viewModel.getAccounts() }
    val accountsResource = observeResource(accountsLiveData)

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    val editLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updated = result.data?.extras?.getSerializableCompat("result", UpdateAccountResponse::class.java)
            if (updated != null) {
                overrides = overrides + (updated.id.toInt() to updated)
            }
        }
    }

    LaunchedEffect(accountsResource?.status) {
        when (accountsResource?.status) {
            Status.ERROR -> showMessage("Something went wrong")
            Status.SUCCESS -> {
                val list = accountsResource.data
                if (list != null) {
                    AccountHolder.accounts = list.map { it.toSimpleAccount() }.toMutableList()
                }
            }
            else -> Unit
        }
    }

    // Mimic previous behavior: reload when screen is resumed.
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                refreshKey += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AccountMonthManipulator(
                monthOffset = MonthSelector.month,
                onPrevious = {
                    MonthSelector.previous()
                    refreshKey += 1
                },
                onCurrent = {
                    MonthSelector.current()
                    refreshKey += 1
                },
                onNext = {
                    if (MonthSelector.month < 0) {
                        MonthSelector.next()
                        refreshKey += 1
                    }
                }
            )

            val rawItems = accountsResource?.data ?: emptyList()
            val items = rawItems
                .map { acc ->
                    overrides[acc.id]?.let { upd ->
                        acc.copy(name = upd.name, moneyAmount = upd.amount)
                    } ?: acc
                }
                .sortedBy { it.name }

            BoxedBody(
                isLoading = accountsResource?.status == Status.LOADING,
                contentPadding = PaddingValues(AccountDefaults.ScreenPadding.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(AccountDefaults.ItemSpacing.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(id = com.example.internetapi.R.string.start_menu_account),
                            style = MaterialTheme.typography.h5
                        )
                    }

                    items(items) { account ->
                        AccountCard(
                            account = account,
                            onOpenDetails = { onOpenDetails(account) },
                            onEdit = { onOpenEdit(account, editLauncher) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxedBody(
    isLoading: Boolean,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        content()
        if (isLoading) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AccountMonthManipulator(
    monthOffset: Int,
    onPrevious: () -> Unit,
    onCurrent: () -> Unit,
    onNext: () -> Unit
) {
    val dateText = remember(monthOffset) {
        LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset.toLong())
            .format(DateFormatter.yyyymm)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPrevious, modifier = Modifier.weight(0.3f)) {
            Text(text = stringResource(id = com.example.internetapi.R.string.previous_month))
        }
        Text(
            text = dateText,
            modifier = Modifier
                .weight(0.4f)
                .padding(horizontal = 12.dp)
                .clickable(onClick = onCurrent),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Button(onClick = onNext, modifier = Modifier.weight(0.3f)) {
            Text(text = stringResource(id = com.example.internetapi.R.string.next_month))
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccountDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(AccountDefaults.CardInnerSpacing.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.SemiBold
                )
                Button(onClick = onEdit) {
                    Text(text = stringResource(id = com.example.internetapi.R.string.edit_account_btn_text))
                }
            }

            Text(
                text = "Stan konta: ${MoneyFormatter.df.format(account.moneyAmount)}",
                style = MaterialTheme.typography.body1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Przychód: ${MoneyFormatter.df.format(account.income)}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Wydatki: ${MoneyFormatter.df.format(account.expense)}",
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
