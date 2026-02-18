package com.example.internetapi.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.functions.getSerializableExtraCompat
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.ui.viewModel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetActivity : AppCompatActivity() {
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    BudgetScreen(
                        viewModel = viewModel,
                        onOpenUpdateBudget = { item, launcher ->
                            Intent(this, UpdateBudgetActivity::class.java).apply {
                                putExtra("budget", item)
                            }.let { launcher.launch(it) }
                        }
                    )
                }
            }
        }
    }
}

private object BudgetDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 16
    const val CardInnerSpacing = 10
}

@Composable
private fun BudgetScreen(
    viewModel: BudgetViewModel,
    onOpenUpdateBudget: (MonthBudget, androidx.activity.result.ActivityResultLauncher<Intent>) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var refreshKey by rememberSaveable { mutableStateOf(0) }
    var overrides by remember { mutableStateOf<Map<Int, UpdateBudgetResponse>>(emptyMap()) }
    var totalPlannedOverride by remember { mutableStateOf<java.math.BigDecimal?>(null) }

    val budgetsLiveData = remember(refreshKey) { viewModel.getBudgets() }
    val budgetsResource = observeResource(budgetsLiveData)

    var recalculateKey by rememberSaveable { mutableStateOf(0) }
    val recalcLiveData = remember(recalculateKey) {
        if (recalculateKey == 0) null else viewModel.recalculateBudgets()
    }
    val recalcResource = observeResource(recalcLiveData)

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updated = result.data?.getSerializableExtraCompat("result", UpdateBudgetResponse::class.java)
            if (updated != null) {
                overrides = overrides + (updated.budgetId to updated)
                totalPlannedOverride = updated.monthPlanned
            }
        }
    }

    LaunchedEffect(budgetsResource?.status) {
        when (budgetsResource?.status) {
            Status.ERROR -> showMessage("Failed to load budgets data")
            Status.SUCCESS -> {
                if (budgetsResource?.data?.budgets?.isEmpty() == true) {
                    showMessage("No data available. Please add new data")
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(recalcResource?.status) {
        when (recalcResource?.status) {
            Status.SUCCESS -> {
                overrides = emptyMap()
                totalPlannedOverride = null
                refreshKey += 1
            }
            Status.ERROR -> showMessage("Failed to recalculate budgets")
            else -> Unit
        }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            UnifiedMonthSwitcher(
                labels = monthSwitcherLabels(monthOffset = MonthSelector.month),
                onPrevious = {
                    MonthSelector.previous()
                    overrides = emptyMap()
                    totalPlannedOverride = null
                    refreshKey += 1
                },
                onCurrent = {
                    if (MonthSelector.month != 0) {
                        MonthSelector.current()
                        overrides = emptyMap()
                        totalPlannedOverride = null
                        refreshKey += 1
                    }
                },
                onNext = {
                    if (MonthSelector.month < 0) {
                        MonthSelector.next()
                        overrides = emptyMap()
                        totalPlannedOverride = null
                        refreshKey += 1
                    }
                }
            )

            val budget = budgetsResource?.data
            val displayedBudgets = budget?.budgets
                ?.map { item ->
                    overrides[item.budgetId]?.let { updated ->
                        item.copy(planned = updated.planned, percentage = updated.percentage)
                    } ?: item
                }
                ?: emptyList()

            val totalEarned = budget?.totalEarned
            val totalSpend = budget?.totalSpend
            val totalPlanned = totalPlannedOverride ?: budget?.totalPlanned

            BudgetTotals(
                totalEarned = totalEarned?.let(df::format) ?: "-",
                totalPlanned = totalPlanned?.let(df::format) ?: "-",
                totalSpend = totalSpend?.let(df::format) ?: "-",
                onRecalculate = { recalculateKey += 1 }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(BudgetDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(BudgetDefaults.ItemSpacing.dp)
            ) {
                if (budgetsResource?.status == Status.LOADING) {
                    item {
                        Text(text = "Loading...", style = MaterialTheme.typography.body1)
                    }
                }

                items(displayedBudgets) { item ->
                    MonthBudgetCard(
                        budget = item,
                        onClick = { onOpenUpdateBudget(item, launcher) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetTotals(
    totalEarned: String,
    totalPlanned: String,
    totalSpend: String,
    onRecalculate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(top = 6.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TotalsRow(label = stringResource(id = com.example.internetapi.R.string.money_earned), value = totalEarned)
        TotalsRow(label = stringResource(id = com.example.internetapi.R.string.money_planned), value = totalPlanned)
        TotalsRow(label = stringResource(id = com.example.internetapi.R.string.money_spend), value = totalSpend)

        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onRecalculate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = com.example.internetapi.R.string.recalculate))
        }
    }
}

@Composable
private fun TotalsRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.body1)
        Text(text = value, style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MonthBudgetCard(
    budget: MonthBudget,
    onClick: () -> Unit
) {
    val spendIsOverPlanned = budget.spent > budget.planned
    val spendColor = if (spendIsOverPlanned) Color.Red else Color(0xFF2E7D32)
    val progressColor = if (spendIsOverPlanned) Color.Red else Color(0xFF1976D2)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BudgetDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(BudgetDefaults.CardInnerSpacing.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.h6
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Zap: ${df.format(budget.planned)}",
                        style = MaterialTheme.typography.body2
                    )
                    Text(
                        text = "Wyd: ${df.format(budget.spent)}",
                        style = MaterialTheme.typography.body2,
                        color = spendColor
                    )
                }
            }

            Text(
                text = "${budget.percentage}%",
                style = MaterialTheme.typography.body2
            )
            LinearProgressIndicator(
                progress = (budget.percentage.coerceIn(0, 100) / 100f),
                modifier = Modifier.fillMaxWidth(),
                color = progressColor
            )
        }
    }
}
