package com.example.internetapi.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.internetapi.R
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.InvoiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.internetapi.config.MoneyFormatter

@AndroidEntryPoint
class InvoiceDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_LOAD_INVOICE_DETAILS: String = "Failed to load invoice details"
    private val invoiceViewModel: InvoiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val invoiceId = intent.extras?.getLong("invoiceId") ?: return

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    InvoiceDetailsScreen(
                        invoiceId = invoiceId,
                        viewModel = invoiceViewModel,
                        errorMessage = FAILED_TO_LOAD_INVOICE_DETAILS
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceDetailsScreen(
    invoiceId: Long,
    viewModel: InvoiceViewModel,
    errorMessage: String,
) {
    val scaffoldState = rememberScaffoldState()

    val liveData = remember(invoiceId) { viewModel.invoiceDetails(invoiceId) }
    val resource = observeResource(liveData)

    LaunchedEffect(resource?.status) {
        if (resource?.status == Status.ERROR) {
            scaffoldState.snackbarHostState.showSnackbar(errorMessage)
        }
    }

    val items = resource?.data.orEmpty()
    val status = resource?.status
    val isLoading = status == null || status == Status.LOADING

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.invoice_details_title),
                        style = MaterialTheme.typography.h5
                    )
                }

                if (!isLoading && items.isEmpty()) {
                    item {
                        Text(text = "Brak pozycji", style = MaterialTheme.typography.body1)
                    }
                } else {
                    items(items, key = { it.invoiceItemId }) { detail ->
                        InvoiceDetailsRow(detail = detail)
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun InvoiceDetailsRow(detail: InvoiceDetails) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = detail.productName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = "Suma: ${MoneyFormatter.df.format(detail.totalPrice)}",
                    style = MaterialTheme.typography.subtitle1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ilość:\n${MoneyFormatter.df.format(detail.quantity)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Cena:\n${MoneyFormatter.df.format(detail.price)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Rabat:\n${MoneyFormatter.df.format(detail.discount)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
