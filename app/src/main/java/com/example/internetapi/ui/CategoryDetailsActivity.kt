package com.example.internetapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log
import com.example.internetapi.models.Category

@AndroidEntryPoint
class CategoryDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_LOAD_CATEGORY_DETAILS = "Failed to load category details data"
    private val TAG = "CategoryDetailsActivity"

    private val viewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val categoryId = intent.extras?.getInt("categoryId") ?: -1
        val categoryName = intent.extras?.getString("name")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CategoryDetailsScreen(
                        viewModel = viewModel,
                        categoryId = categoryId,
                        title = categoryName
                    )
                }
            }
        }
    }
}

private object CategoryDetailsDefaults {
    const val ScreenPadding = 16
    const val ItemSpacing = 12
    const val CardPadding = 16
    const val CardInnerSpacing = 10
}

@Composable
private fun CategoryDetailsScreen(
    viewModel: CategoryViewModel,
    categoryId: Int,
    title: String?
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    var refreshKey by rememberSaveable(categoryId) { mutableStateOf(0) }
    val detailsLiveData = remember(categoryId, refreshKey) { viewModel.getCategoryDetails(categoryId) }
    val detailsResource = observeResource(detailsLiveData)

    var changeCategoryFor by remember { mutableStateOf<CategoryDetails?>(null) }
    var categoriesKey by rememberSaveable { mutableStateOf(0) }
    val categoriesLiveData = remember(categoriesKey) {
        if (categoriesKey == 0) null else viewModel.getCategories()
    }
    val categoriesResource = observeResource(categoriesLiveData)

    LaunchedEffect(detailsResource?.status) {
        when (detailsResource?.status) {
            Status.ERROR -> showMessage("Failed to load category details data")
            else -> Unit
        }
    }

    LaunchedEffect(categoriesResource?.status) {
        when (categoriesResource?.status) {
            Status.ERROR -> showMessage("Failed to load categories")
            else -> Unit
        }
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val items = detailsResource?.data ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(CategoryDetailsDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(CategoryDetailsDefaults.ItemSpacing.dp)
            ) {
                item {
                    Text(
                        text = title ?: "Category details",
                        style = MaterialTheme.typography.h5
                    )
                }

                items(items, key = { it.assortmentId }) { detail ->
                    CategoryDetailCard(
                        detail = detail,
                        onChangeCategory = {
                            changeCategoryFor = detail
                            categoriesKey += 1
                        },
                        onShowMonthExpenses = {
                            showMessage("Not implemented")
                        }
                    )
                }
            }

            if (detailsResource?.status == Status.LOADING) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    val pendingChange = changeCategoryFor
    if (pendingChange != null) {
        val categories = categoriesResource?.data?.sortedBy { it.name } ?: emptyList()
        val isLoading = categoriesResource?.status == Status.LOADING
        var selected by remember(pendingChange.assortmentId, categories) {
            mutableStateOf(categories.firstOrNull { it.id == 1 } ?: categories.firstOrNull())
        }
        var expanded by remember(pendingChange.assortmentId) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { changeCategoryFor = null },
            title = { Text(text = "Change category") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = pendingChange.name)

                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(2.dp))
                            Text(text = "Loading categories...")
                        }
                    } else if (categories.isEmpty()) {
                        Text(text = "No categories")
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = selected?.name ?: "Select category",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .padding(12.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(onClick = {
                                        selected = cat
                                        expanded = false
                                    }) {
                                        Text(text = cat.name)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedId = selected?.id
                        Log.i(
                            "CategoryDetailsActivity",
                            "Change category confirmed for itemId=${pendingChange.assortmentId}, categoryId=$selectedId"
                        )
                        changeCategoryFor = null
                    },
                    enabled = !isLoading && selected != null
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { changeCategoryFor = null }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryDetailCard(
    detail: CategoryDetails,
    onChangeCategory: () -> Unit,
    onShowMonthExpenses: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CategoryDetailsDefaults.CardPadding.dp),
            verticalArrangement = Arrangement.spacedBy(CategoryDetailsDefaults.CardInnerSpacing.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = detail.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.h6
                )

                CategoryDetailOverflowMenu(
                    onChangeCategory = onChangeCategory,
                    onShowMonthExpenses = onShowMonthExpenses
                )
            }

            Text(
                text = detail.price.toString(),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun CategoryDetailOverflowMenu(
    onChangeCategory: () -> Unit,
    onShowMonthExpenses: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            text = "...",
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                expanded = false
                onChangeCategory()
            }) {
                Text(text = "Change category")
            }
            DropdownMenuItem(onClick = {
                expanded = false
                onShowMonthExpenses()
            }) {
                Text(text = "Show month expenses")
            }
        }
    }
}
