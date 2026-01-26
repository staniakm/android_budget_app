package com.example.internetapi.ui

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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.internetapi.config.DateFormatter
import com.example.internetapi.config.MoneyFormatter.df
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Category
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

private object CategoryDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 16
    const val CardInnerSpacing = 10
}

@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {
    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CategoryScreen(
                        viewModel = viewModel,
                        onOpenDetails = { item ->
                            Intent(this, CategoryDetailsActivity::class.java)
                                .apply {
                                    putExtra("name", item.name)
                                    putExtra("categoryId", item.id)
                                }
                                .let { startActivity(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryScreen(
    viewModel: CategoryViewModel,
    onOpenDetails: (Category) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var refreshKey by remember { mutableStateOf(0) }
    val categoriesLiveData = remember(refreshKey) { viewModel.getCategories() }
    val categoriesResource by categoriesLiveData.observeAsState()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(categoriesResource?.status) {
        when (categoriesResource?.status) {
            Status.ERROR -> showMessage("Failed to load categories data")
            Status.SUCCESS -> {
                if (categoriesResource?.data?.isEmpty() == true) {
                    showMessage("No data available. Please add new data")
                }
            }
            else -> Unit
        }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthManipulator(
                monthOffset = MonthSelector.month,
                onPrevious = {
                    MonthSelector.previous()
                    refreshKey += 1
                },
                onCurrent = {
                    MonthSelector.current()
                    refreshKey += 1
                },
                onNext = {
                    if (MonthSelector.month < 0) {
                        MonthSelector.next()
                        refreshKey += 1
                    }
                }
            )

            val items = categoriesResource?.data ?: emptyList()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(CategoryDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(CategoryDefaults.ItemSpacing.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = com.example.internetapi.R.string.title_activity_category),
                        style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(items) { category ->
                    CategoryCard(category = category, onClick = { onOpenDetails(category) })
                }
            }
        }
    }
}

@Composable
private fun MonthManipulator(
    monthOffset: Int,
    onPrevious: () -> Unit,
    onCurrent: () -> Unit,
    onNext: () -> Unit
) {
    val dateText = remember(monthOffset) {
        LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset.toLong())
            .format(DateFormatter.yyyymm)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPrevious, modifier = Modifier.weight(0.3f)) {
            Text(text = stringResource(id = com.example.internetapi.R.string.previous_month))
        }
        Text(
            text = dateText,
            modifier = Modifier
                .weight(0.4f)
                .padding(horizontal = 12.dp)
                .clickable(onClick = onCurrent),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h6
        )
        Button(onClick = onNext, modifier = Modifier.weight(0.3f)) {
            Text(text = stringResource(id = com.example.internetapi.R.string.next_month))
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CategoryDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(CategoryDefaults.CardInnerSpacing.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.h6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ostatni miesiąc:\n${df.format(category.monthSummary)}",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "Rok:\n${df.format(category.yearSummary)}",
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}
