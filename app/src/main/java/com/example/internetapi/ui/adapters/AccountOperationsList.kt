package com.example.internetapi.ui.adapters

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.internetapi.models.AccountOperation

@Composable
fun AccountOperationsList(
    operations: List<AccountOperation>,
    onItemClick: (item: AccountOperation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState()
    ) {
        items(count = operations.size) { index ->
            operations[index].also { accountOperation ->
                AccountOperationCard(item = accountOperation, onItemClick = { onItemClick(it) })
            }
        }
    }
}