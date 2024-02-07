package com.example.internetapi.ui.adapters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.Account
import java.math.BigDecimal

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AccountInfo(
    item: Account,
    surfaceClick: () -> Unit,
    editAccountClick: () -> Unit
) {
    Surface(
        onClick = surfaceClick
    ) {
        Column {
            HeaderRow(name = item.name, editAccountClick)
            AccountTotal(item.moneyAmount)
            IncomeOutcome(item = item)
        }
    }
}

@Composable
private fun HeaderRow(name: String, edit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        AccountName(name = name, Modifier.weight(0.7f))
        Button(
            onClick = { edit() },
            shape = CircleShape,
            modifier = Modifier
                .size(20.dp)
                .weight(0.3f),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                "Edytuj",
                fontSize = 15.sp,
            )
        }

    }
}

@Composable
private fun AccountName(name: String, modifier: Modifier) {
    Text(
        text = name,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Composable
private fun AccountTotal(moneyAmount: BigDecimal) {
    Text(
        text = "Stan konta: ${MoneyFormatter.df.format(moneyAmount)}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        fontSize = 16.sp
    )
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            fontSize = 16.sp
        )
        Text(
            text = "Wydatki: ${MoneyFormatter.df.format(item.expense)}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
private fun AccountInfoPreview() {
    MaterialTheme {
        AccountInfo(
            item = Account(
                1, "Account name",
                BigDecimal(2.32),
                BigDecimal(2.33),
                BigDecimal(3.44)
            ),
            surfaceClick = { println("Surface click") },
            editAccountClick = { println("Click button") }
        )
    }
}