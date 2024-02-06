package com.example.internetapi.ui.adapters

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.Account
import java.math.BigDecimal

@Composable
fun AccountInfo(item: Account) {
    Surface {
        IncomeOutcome(item = item)
    }
}

@Composable
private fun IncomeOutcome(item: Account) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            text = "Przychód: ${MoneyFormatter.df.format(item.income)}",
            modifier = Modifier.fillMaxWidth().weight(1f),
            fontSize = 16.sp
        )
        Text(
            text = "Wydatki: ${MoneyFormatter.df.format(item.expense)}",
            modifier = Modifier.fillMaxWidth().weight(1f),
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
private fun IncomeOutcomePreview() {
    MaterialTheme {
        IncomeOutcome(
            item = Account(
                1, "Account name",
                BigDecimal(2.32),
                BigDecimal(2.33),
                BigDecimal(3.44)
            )
        )
    }
}