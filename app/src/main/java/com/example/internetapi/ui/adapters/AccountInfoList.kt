package com.example.internetapi.ui.adapters

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.internetapi.models.Account

@Composable
fun AccountInfoList(
    accounts: List<Account>,
    surfaceClick: (account: Account) -> Unit,
    editAccountClick: (account:Account) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        items(count = accounts.size) { index ->
            accounts[index].also {
                AccountInfo(
                    item = it,
                    surfaceClick = surfaceClick,
                    editAccountClick = editAccountClick
                )
            }
        }
    }
}