package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.internetapi.constant.Constant
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(shouldLoadMenu = ::shouldLoadMenu)
                }
            }
        }
    }

    private fun shouldLoadMenu(): Boolean {
        val preferences = getSharedPreferences("accountSharedPreferences", MODE_PRIVATE)
        return preferences.getString("hostAddress", "")?.let {
            Constant.BASE_URL = it
            it.isNotBlank()
        } ?: false
    }
}

@Composable
private fun MainScreen(
    shouldLoadMenu: () -> Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showMainMenu by remember { mutableStateOf(shouldLoadMenu()) }

    DisposableEffect(lifecycleOwner, shouldLoadMenu) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                showMainMenu = shouldLoadMenu()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        bottomBar = {
            BottomSettingsBar {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.h5
            )

            if (!showMainMenu) {
                Text(
                    text = "Ustaw adres serwera w Ustawieniach, aby odblokowac menu.",
                    style = MaterialTheme.typography.body1
                )
            } else {
                MenuCard(title = "Konta") {
                    context.startActivity(Intent(context, AccountActivity::class.java))
                }
                MenuCard(title = "Budżet") {
                    context.startActivity(Intent(context, BudgetActivity::class.java))
                }
                MenuCard(title = "Wykresy") {
                    context.startActivity(Intent(context, ChartActivity::class.java))
                }
                MenuCard(title = "Media") {
                    context.startActivity(Intent(context, MediaActivity::class.java))
                }
                MenuCard(title = "Zarządzanie") {
                    context.startActivity(Intent(context, CategoryActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BottomSettingsBar(
    onClick: () -> Unit
) {
    Surface(elevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuCard(title = "Ustawienia", contentPadding = 14.dp, onClick = onClick)
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DisplayPreviewMenu() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            MainScreen(shouldLoadMenu = { true })
        }
    }
}
