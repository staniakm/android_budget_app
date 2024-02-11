package com.example.internetapi.ui.adapters

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.internetapi.R
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.AccountOperation
import com.example.internetapi.ui.theme.InternetApiTheme
import java.math.BigDecimal

@Composable
fun AccountOperationCard(item: AccountOperation) {
    Surface(
        modifier = Modifier
            .border(width = 1.dp, color = Color.LightGray)
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Text(
                item.date, modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )
            Text(
                text = MoneyFormatter.df.format(item.value),
                Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
            )
            ValueIcon(item.type, modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f))

        }
    }
}

@Composable
private fun ValueIcon(type: String, modifier: Modifier) {
    when (type == "INCOME") {
        true -> Icon(
            painter = painterResource(id = R.drawable.income), contentDescription = "",
            tint = Color.Green,
            modifier = modifier
        )

        else ->
            Icon(
                painter = painterResource(id = R.drawable.outcome), contentDescription = "",
                tint = Color.Red,
                modifier = modifier
            )
    }
}

@Preview
@Composable
private fun AccountOperationCardPreviewDark() {
    InternetApiTheme(darkTheme = true) {
        AccountOperationCard(
            item = AccountOperation(
                id = 1L,
                date = "2023-01-02",
                value = BigDecimal("23.45"),
                account = 1,
                type = "OUTCOME"
            )
        )
    }
}

@Preview
@Composable
private fun AccountOperationCardPreviewLight() {
    InternetApiTheme(darkTheme = false) {
        AccountOperationCard(
            item = AccountOperation(
                id = 1L,
                date = "2023-01-02",
                value = BigDecimal("23.45"),
                account = 1,
                type = "INCOME"
            )
        )
    }
}