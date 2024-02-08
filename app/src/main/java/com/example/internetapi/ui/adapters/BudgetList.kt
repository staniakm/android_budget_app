package com.example.internetapi.ui.adapters

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.internetapi.models.MonthBudget

@Composable
fun BudgetList(budgets: List<MonthBudget>,
               onCardClick:(monthBudget: MonthBudget)->Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        items(count = budgets.size) { index ->
            val budget = budgets[index]
            BudgetSummaryCard(item = budget) { onCardClick(budget) }
        }
    }
}