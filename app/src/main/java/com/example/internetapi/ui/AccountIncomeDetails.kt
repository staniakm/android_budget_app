package com.example.internetapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.internetapi.R
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.AccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountIncomeDetails : AppCompatActivity() {

    private val FAILED_TO_LOAD_ACCOUNT_INCOME by lazy { getString(R.string.error_failed_load_account_income) }

    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extra = intent.extras
        val incomeSum = extra?.getString("income", "0.0") ?: "0.0"
        val name = extra?.getString("name", "") ?: ""
        val accountId = extra?.getInt("accountId") ?: -1

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AccountIncomeDetailsScreen(
                        viewModel = accountViewModel,
                        accountId = accountId,
                        name = name,
                        incomeSum = incomeSum,
                        failedMessage = FAILED_TO_LOAD_ACCOUNT_INCOME
                    )
                }
            }
        }
    }
}

private object AccountIncomeDetailsDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 14
}

@Composable
private fun AccountIncomeDetailsScreen(
    viewModel: AccountViewModel,
    accountId: Int,
    name: String,
    incomeSum: String,
    failedMessage: String,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val missingAccountIdMessage = stringResource(id = R.string.error_missing_account_id)

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    val incomeLiveData = remember(accountId) {
        if (accountId <= 0) null else viewModel.getAccountIncome(accountId)
    }
    val incomeResource = observeResource(incomeLiveData)

    LaunchedEffect(incomeResource?.status, accountId) {
        if (accountId <= 0) showMessage(missingAccountIdMessage)
        if (incomeResource?.status == Status.ERROR) showMessage(failedMessage)
    }

    val isLoading = incomeResource?.status == Status.LOADING
    val items = incomeResource?.data ?: emptyList()

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AccountIncomeDetailsDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(AccountIncomeDetailsDefaults.ItemSpacing.dp)
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
                                .padding(AccountIncomeDetailsDefaults.CardPadding.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = com.example.internetapi.R.string.income),
                                modifier = Modifier.weight(0.3f),
                                style = MaterialTheme.typography.h6
                            )
                            Text(
                                text = incomeSum,
                                modifier = Modifier.weight(0.7f),
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }

                items(items, key = { it.id }) { income ->
                    AccountIncomeCard(income = income)
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AccountIncomeCard(income: AccountIncome) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccountIncomeDetailsDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = income.description,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = income.date,
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = df.format(income.income),
                    modifier = Modifier.weight(0.4f),
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
