package com.example.internetapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.internetapi.R
import com.example.internetapi.functions.getSerializableCompat
import com.example.internetapi.models.Account
import com.example.internetapi.models.UpdateAccountRequest
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

import android.content.Intent
import com.example.internetapi.models.UpdateAccountResponse
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AccountUpdateActivity : AppCompatActivity() {
    private val accountViewModel: AccountViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val acc = intent.extras?.getSerializableCompat("account", Account::class.java)
        if (acc == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AccountUpdateScreen(
                        account = acc,
                        viewModel = accountViewModel,
                        initialName = acc.name,
                        initialMoney = acc.moneyAmount.toString(),
                        onFinished = ::updateAdapter
                    )
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

@Composable
private fun AccountUpdateScreen(
    account: Account,
    viewModel: AccountViewModel,
    initialName: String,
    initialMoney: String,
    onFinished: (UpdateAccountResponse?) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var moneyText by remember(initialMoney) { mutableStateOf(initialMoney) }
    val emptyAccountNameMessage = stringResource(id = R.string.error_empty_account_name)
    val invalidAmountMessage = stringResource(id = R.string.error_invalid_amount)
    val failedUpdateAccountMessage = stringResource(id = R.string.error_failed_update_account_data)

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var requestKey by rememberSaveable(account.id) { mutableStateOf(0) }
    var updateRequest by remember { mutableStateOf<UpdateAccountRequest?>(null) }
    val updateLiveData = remember(requestKey) {
        val request = updateRequest
        if (requestKey == 0 || request == null) null else viewModel.updateAccount(account.id, request)
    }
    val updateResource = observeResource(updateLiveData)

    fun show(message: String) {
        scope.launch {
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(updateResource?.status) {
        when (updateResource?.status) {
            com.example.internetapi.models.Status.SUCCESS -> onFinished(updateResource.data)
            com.example.internetapi.models.Status.ERROR -> show(failedUpdateAccountMessage)
            else -> Unit
        }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.account_name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = moneyText,
                onValueChange = { moneyText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = stringResource(id = R.string.account_money)) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (name.isBlank()) {
                        show(emptyAccountNameMessage)
                        return@Button
                    }

                    val money = parseMoneyOrNull(moneyText)
                    if (money == null) {
                        show(invalidAmountMessage)
                        return@Button
                    }

                    updateRequest = UpdateAccountRequest(
                        id = account.id.toLong(),
                        name = name,
                        newMoneyAmount = money
                    )
                    requestKey += 1
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}

private fun parseMoneyOrNull(raw: String): BigDecimal? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null

    // Accept both ',' and '.' as decimal separators; ignore spaces as group separators.
    // Also tolerates formats like "1 234,56" or "1,234.56" by using the last separator.
    val noSpaces = trimmed
        .replace(" ", "")
        .replace("\u00A0", "")

    val (sign, unsigned) = when {
        noSpaces.startsWith('-') -> "-" to noSpaces.drop(1)
        noSpaces.startsWith('+') -> "" to noSpaces.drop(1)
        else -> "" to noSpaces
    }

    if (unsigned.isEmpty()) return null

    val lastDot = unsigned.lastIndexOf('.')
    val lastComma = unsigned.lastIndexOf(',')
    val sepIndex = maxOf(lastDot, lastComma)

    val (intPartRaw, fracPartRaw) = if (sepIndex >= 0) {
        unsigned.substring(0, sepIndex) to unsigned.substring(sepIndex + 1)
    } else {
        unsigned to ""
    }

    val intDigits = intPartRaw.filter { it.isDigit() }
    val fracDigits = fracPartRaw.filter { it.isDigit() }

    if (intDigits.isEmpty() && fracDigits.isEmpty()) return null

    val normalized = buildString {
        append(sign)
        append(if (intDigits.isEmpty()) "0" else intDigits)
        if (fracDigits.isNotEmpty()) {
            append('.')
            append(fracDigits)
        }
    }

    return normalized.toBigDecimalOrNull()
}
