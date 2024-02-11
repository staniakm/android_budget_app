package com.example.internetapi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountSummary(
    accountName: String,
    income: String,
    outcome: String,
    incomeRowClick: () -> Unit,
    outcomeRowClick: () -> Unit
) {
    Surface(

    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AccountName(accountName)
            SummaryRow("Przychody", income, incomeRowClick)
            SummaryRow(name = "Wydatki", value = outcome, outcomeRowClick)
        }
    }
}

@Composable
private fun SummaryRow(name: String, value: String, rowClick: () -> Unit) {
    Row(modifier = Modifier.clickable { rowClick() }) {
        Text(
            text = name, modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .weight(0.3f)
        )
        Text(
            text = value, modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )
    }
}

@Composable
private fun AccountName(accountName: String) {
    Text(
        text = accountName,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(5.dp)
    )
}