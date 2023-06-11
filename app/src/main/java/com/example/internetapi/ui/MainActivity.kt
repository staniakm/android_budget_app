package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.internetapi.constant.Constant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                LoadMenu(shouldLoadMenu())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                LoadMenu(shouldLoadMenu())
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
private fun LoadMenu(visibility: Boolean = false) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (visibility) {
            ClickableMenuItem(name = "Konta",
                Modifier
                    .clickable {
                        val intent = Intent(context, AccountActivity::class.java)
                        context.startActivity(intent)
                    })
            ClickableMenuItem(name = "Budżet",
                Modifier.clickable {
                    val intent = Intent(context, BudgetActivity::class.java)
                    context.startActivity(intent)
                })
            ClickableMenuItem(name = "Wykresy",
                Modifier.clickable {
                    val intent = Intent(context, ChartActivity::class.java)
                    context.startActivity(intent)
                })
            ClickableMenuItem(name = "Media",
                Modifier.clickable {
                    val intent = Intent(context, MediaActivity::class.java)
                    context.startActivity(intent)
                })
            ClickableMenuItem(name = "Zarządzanie",
                Modifier.clickable {
                    val intent = Intent(context, CategoryActivity::class.java)
                    context.startActivity(intent)
                })
        }
        Spacer(modifier = Modifier.weight(1f))
        ClickableMenuItem(
            name = "Ustawienia",
            Modifier
                .padding(5.dp)
                .clickable {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                }
        )
    }
}

@Composable
private fun ClickableMenuItem(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        modifier = modifier,
        fontSize = 35.sp
    )
}

@Preview(showBackground = true)
@Composable
private fun DisplayPreviewMenu() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        LoadMenu(true)
    }
}