package com.example.internetapi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.internetapi.config.DateFormatter
import java.time.LocalDate

data class MonthSwitcherLabels(
    val previous: String,
    val current: String,
    val next: String,
)

fun monthSwitcherLabels(baseDate: LocalDate = LocalDate.now(), monthOffset: Int): MonthSwitcherLabels {
    val date = baseDate.withDayOfMonth(1).plusMonths(monthOffset.toLong())
    return MonthSwitcherLabels(
        previous = date.minusMonths(1).format(DateFormatter.yyyymm),
        current = date.format(DateFormatter.yyyymm),
        next = date.plusMonths(1).format(DateFormatter.yyyymm),
    )
}

@Composable
fun UnifiedMonthSwitcher(
    labels: MonthSwitcherLabels,
    onPrevious: () -> Unit,
    onCurrent: () -> Unit,
    onNext: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPrevious) { Text(text = labels.previous) }
            TextButton(onClick = onCurrent) {
                Text(text = labels.current, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.button)
            }
            TextButton(onClick = onNext) { Text(text = labels.next) }
        }
    }
}
