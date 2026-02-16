package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.*
import com.example.internetapi.ui.viewModel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class UpdateBudgetActivity : AppCompatActivity() {
    private val TAG: String = "UpdateBudgetActivity"

    private val budgetViewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val budget = intent.extras?.getSerializable("budget") as? MonthBudget
        if (budget == null) {
            Log.e(TAG, "Missing MonthBudget in extras under key 'budget'")
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
                    UpdateBudgetScreen(
                        viewModel = budgetViewModel,
                        budget = budget,
                        onFinished = { result ->
                            if (result != null) {
                                val data = Intent().apply { putExtra("result", result) }
                                setResult(RESULT_OK, data)
                            } else {
                                setResult(RESULT_CANCELED)
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }
}

private object UpdateBudgetDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 14
}

@Composable
private fun UpdateBudgetScreen(
    viewModel: BudgetViewModel,
    budget: MonthBudget,
    onFinished: (UpdateBudgetResponse?) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    val itemsLiveData = remember(budget.budgetId) { viewModel.getBudgetItems(budget.budgetId) }
    val itemsResource = observeResource(itemsLiveData)

    var plannedDialogOpen by rememberSaveable(budget.budgetId) { mutableStateOf(false) }
    var plannedText by rememberSaveable(budget.budgetId) { mutableStateOf(df.format(budget.planned)) }

    var updateKey by rememberSaveable(budget.budgetId) { mutableStateOf(0) }
    var updatePlanned by remember { mutableStateOf<BigDecimal?>(null) }
    val updateLiveData = remember(updateKey) {
        val planned = updatePlanned
        if (updateKey == 0 || planned == null) null
        else viewModel.updateBudget(UpdateBudgetRequest(budget.budgetId, planned))
    }
    val updateResource = observeResource(updateLiveData)

    LaunchedEffect(itemsResource?.status) {
        if (itemsResource?.status == Status.ERROR) showMessage("Unable to load budget items")
    }

    LaunchedEffect(updateResource?.status) {
        when (updateResource?.status) {
            Status.ERROR -> showMessage("Failed to update budget data")
            Status.SUCCESS -> onFinished(updateResource?.data)
            else -> Unit
        }
    }

    val listItems = itemsResource?.data ?: emptyList()
    val isLoadingItems = itemsResource?.status == Status.LOADING
    val isUpdating = updateResource?.status == Status.LOADING

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(UpdateBudgetDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(UpdateBudgetDefaults.ItemSpacing.dp)
            ) {
                item {
                    BudgetHeader(
                        budget = budget,
                        onEditPlanned = {
                            plannedText = df.format(budget.planned)
                            plannedDialogOpen = true
                        }
                    )
                }

                items(listItems, key = { it.invoiceItemId }) { item ->
                    BudgetItemCard(item = item)
                }
            }

            if (isLoadingItems || isUpdating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (plannedDialogOpen) {
        AlertDialog(
            onDismissRequest = { plannedDialogOpen = false },
            title = { Text(text = "Update planned value for budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Spend: ${df.format(budget.spent)}")
                    OutlinedTextField(
                        value = plannedText,
                        onValueChange = { plannedText = it },
                        label = { Text(text = "Planned") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUpdating
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val raw = plannedText.trim()
                        val value = raw.replace(',', '.').toBigDecimalOrNull()
                        if (value == null) {
                            showMessage("Provided value: $raw - is not parsable to number")
                            return@TextButton
                        }
                        updatePlanned = value
                        updateKey += 1
                        plannedDialogOpen = false
                    },
                    enabled = !isUpdating
                ) {
                    Text(text = stringResource(id = com.example.internetapi.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { plannedDialogOpen = false },
                    enabled = !isUpdating
                ) {
                    Text(text = stringResource(id = com.example.internetapi.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun BudgetHeader(
    budget: MonthBudget,
    onEditPlanned: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UpdateBudgetDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = budget.category,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = com.example.internetapi.R.string.money_spend), modifier = Modifier.weight(0.4f))
                Text(text = df.format(budget.spent), modifier = Modifier.weight(0.6f))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = com.example.internetapi.R.string.budget_percentage_used),
                    modifier = Modifier.weight(0.4f)
                )
                Text(text = "${budget.percentage} %", modifier = Modifier.weight(0.6f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = com.example.internetapi.R.string.money_planned),
                    modifier = Modifier.weight(0.4f)
                )
                Text(text = df.format(budget.planned), modifier = Modifier.weight(0.3f))
                Button(
                    onClick = onEditPlanned,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Text(text = stringResource(id = com.example.internetapi.R.string.edit))
                }
            }
        }
    }
}

@Composable
private fun BudgetItemCard(item: InvoiceDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.productName,
                modifier = Modifier.weight(0.28f),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = df.format(item.totalPrice),
                modifier = Modifier.weight(0.24f),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = df.format(item.quantity),
                modifier = Modifier.weight(0.24f),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = df.format(item.price),
                modifier = Modifier.weight(0.24f),
                style = MaterialTheme.typography.body1
            )
        }
    }
}
