package com.example.internetapi.ui.adapters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internetapi.config.MoneyFormatter
import com.example.internetapi.models.MonthBudget
import java.math.BigDecimal


@Composable
fun BudgetSummaryCard(item: MonthBudget, surfaceClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { surfaceClick() },
//        elevation = CardElevation.,
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Column {
            BudgetName(item.category)
            PlannedSpend(item.planned, item.spent)
            ColorProgressBar(progress = item.percentage)
        }
    }
}

@Composable
private fun BudgetName(name: String) {
    Text(
        text = name,
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
            .padding(5.dp)
    )
}

@Composable
private fun PlannedSpend(planned: BigDecimal, spent: BigDecimal) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            text = "Planowane: ${MoneyFormatter.df.format(planned)}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            fontSize = 16.sp
        )
        Text(
            text = "Wydane: ${MoneyFormatter.df.format(spent)}",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            fontSize = 16.sp,
            color = if (spent > planned) {
                Color.Red
            } else {
                Color.Unspecified
            }
        )
    }

}

@Composable
private fun ColorProgressBar(progress: Int) {
    Box(
        modifier = Modifier
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .drawWithContent {
                with(drawContext.canvas.nativeCanvas) {
                    val checkPoint = saveLayer(null, null)
                    val color = when {
                        progress in 0..50 -> Color.Green
                        progress in 51..70 -> Color.Blue
                        else -> Color.Red
                    }
                    // Destination
                    drawContent()

                    // Source
                    drawRect(
                        color = color,
                        size = Size(size.width * progress / 100, size.height),
                        blendMode = BlendMode.Xor
                    )
                    restoreToCount(checkPoint)
                }
            }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "${(progress)}%", color = Color.LightGray, fontSize = 16.sp)
    }
}

@Preview
@Composable
private fun BudgetSummaryCardPreview() {
    MaterialTheme {
        BudgetSummaryCard(
            item = MonthBudget(
                1,
                category = "Budget category",
                spent = BigDecimal(123.33),
                planned = BigDecimal(200),
                percentage = 150
            ), {}
        )
    }
}
