package com.example.internetapi.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberDismissState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.DatePicker
import com.example.internetapi.R
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.MediaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class MediaDetailsActivity : AppCompatActivity() {
    private val FAILED_TO_REMOVE_MEDIA_USAGE = "Failed to remove media usage"
    private val FAILED_TO_ADD_MEDIA_USAGE = "Failed to add media usage entry"
    private val FAILED_TO_LOAD_MEDIA_USAGE_DATA = "Failed to load media usage data"
    private val MEDIA_USAGE_REMOVED = "Media usage item removed"

    private val viewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extra = intent.extras
        val mediaTypeId = extra?.getInt("mediaId") ?: -1
        val name = extra?.getString("name")

        setContent {
            MaterialTheme {
                androidx.compose.material.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MediaDetailsScreen(
                        viewModel = viewModel,
                        mediaTypeId = mediaTypeId,
                        title = name,
                        failedToLoadMessage = FAILED_TO_LOAD_MEDIA_USAGE_DATA,
                        failedToAddMessage = FAILED_TO_ADD_MEDIA_USAGE,
                        failedToRemoveMessage = FAILED_TO_REMOVE_MEDIA_USAGE,
                        removedMessage = MEDIA_USAGE_REMOVED
                    )
                }
            }
        }
    }
}

private object MediaDetailsDefaults {
    const val ScreenPadding = 12
    const val ItemSpacing = 10
    const val CardPadding = 12
}

private data class PendingRemoval(
    val index: Int,
    val item: MediaUsage,
)

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun MediaDetailsScreen(
    viewModel: MediaViewModel,
    mediaTypeId: Int,
    title: String?,
    failedToLoadMessage: String,
    failedToAddMessage: String,
    failedToRemoveMessage: String,
    removedMessage: String,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    var refreshKey by rememberSaveable(mediaTypeId) { mutableStateOf(0) }
    val usageLiveData = remember(mediaTypeId, refreshKey) {
        if (mediaTypeId <= 0) null else viewModel.getMediaUsageByType(mediaTypeId)
    }
    val usageResource = observeResource(usageLiveData)

    var items by remember { mutableStateOf<List<MediaUsage>>(emptyList()) }
    LaunchedEffect(usageResource?.data) {
        usageResource?.data?.let { items = it }
    }

    LaunchedEffect(usageResource?.status, mediaTypeId) {
        if (mediaTypeId <= 0) showMessage("Missing mediaId")
        if (usageResource?.status == Status.ERROR) showMessage(failedToLoadMessage)
    }

    var showAddDialog by rememberSaveable(mediaTypeId) { mutableStateOf(false) }
    var valueText by rememberSaveable(mediaTypeId) { mutableStateOf("0.0") }
    var selectedYear by rememberSaveable(mediaTypeId) { mutableStateOf(java.time.LocalDate.now().year) }
    var selectedMonth by rememberSaveable(mediaTypeId) { mutableStateOf(java.time.LocalDate.now().monthValue) }
    var selectedDay by rememberSaveable(mediaTypeId) { mutableStateOf(java.time.LocalDate.now().dayOfMonth) }

    var addKey by rememberSaveable(mediaTypeId) { mutableStateOf(0) }
    var addRequest by remember { mutableStateOf<MediaRegisterRequest?>(null) }
    val addLiveData = remember(addKey) {
        val req = addRequest
        if (addKey == 0 || req == null) null else viewModel.addMediaUsageEntry(req)
    }
    val addResource = observeResource(addLiveData)

    LaunchedEffect(addResource?.status) {
        when (addResource?.status) {
            Status.SUCCESS -> {
                addResource.data?.let { items = it }
                showMessage("Added")
                addRequest = null
            }
            Status.ERROR -> {
                showMessage(failedToAddMessage)
                addRequest = null
            }
            else -> Unit
        }
    }

    var pendingRemoval by remember { mutableStateOf<PendingRemoval?>(null) }
    var removeKey by rememberSaveable(mediaTypeId) { mutableStateOf(0) }
    var removeId by remember { mutableStateOf<Int?>(null) }
    val removeLiveData = remember(removeKey) {
        val id = removeId
        if (removeKey == 0 || id == null) null else viewModel.removeMediaUsage(id)
    }
    val removeResource = observeResource(removeLiveData)

    LaunchedEffect(removeResource?.status) {
        when (removeResource?.status) {
            Status.SUCCESS -> {
                showMessage(removedMessage)
                pendingRemoval = null
                removeId = null
            }
            Status.ERROR -> {
                showMessage(failedToRemoveMessage)
                val pr = pendingRemoval
                if (pr != null && items.none { it.id == pr.item.id }) {
                    items = items.toMutableList().apply {
                        add(pr.index.coerceIn(0, size), pr.item)
                    }
                }
                pendingRemoval = null
                removeId = null
            }
            else -> Unit
        }
    }

    val isLoading = usageResource?.status == Status.LOADING || addResource?.status == Status.LOADING || removeResource?.status == Status.LOADING

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MediaDetailsDefaults.ScreenPadding.dp),
                verticalArrangement = Arrangement.spacedBy(MediaDetailsDefaults.ItemSpacing.dp)
            ) {
                item {
                    Text(
                        text = title ?: "Media details",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { state ->
                            if (state == DismissValue.DismissedToStart || state == DismissValue.DismissedToEnd) {
                                pendingRemoval = PendingRemoval(index = index, item = item)
                                items = items.toMutableList().apply { removeAt(index) }
                                removeId = item.id
                                removeKey += 1
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFFE0E0))
                                    .padding(16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = "Remove", color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissContent = {
                            val previous = items.getOrNull(index + 1)?.meterRead ?: BigDecimal.ZERO
                            MediaUsageCard(item = item, previous = previous)
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "Add meter value") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth(),
                        factory = { context ->
                            DatePicker(context).apply {
                                init(selectedYear, selectedMonth - 1, selectedDay) { _, y, m, d ->
                                    selectedYear = y
                                    selectedMonth = m + 1
                                    selectedDay = d
                                }
                            }
                        },
                        update = { picker ->
                            if (picker.year != selectedYear || picker.month != selectedMonth - 1 || picker.dayOfMonth != selectedDay) {
                                picker.updateDate(selectedYear, selectedMonth - 1, selectedDay)
                            }
                        }
                    )

                    OutlinedTextField(
                        value = valueText,
                        onValueChange = { valueText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val raw = valueText.trim()
                        val value = raw.replace(',', '.').toBigDecimalOrNull()
                        if (value == null) {
                            Log.w("MediaDetails", "Meter value is not parsable to BigDecimal")
                            showMessage("Provided value: $raw - is not parsable to number")
                            return@TextButton
                        }
                        addRequest = MediaRegisterRequest(mediaTypeId, value, selectedYear, selectedMonth)
                        addKey += 1
                        showAddDialog = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun MediaUsageCard(
    item: MediaUsage,
    previous: BigDecimal,
) {
    val change = item.meterRead.minus(previous)
    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MediaDetailsDefaults.CardPadding.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.month}-${item.year}",
                modifier = Modifier.weight(0.22f),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = change.toString(),
                modifier = Modifier.weight(0.24f),
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.meterRead.toString(),
                modifier = Modifier.weight(0.25f),
                style = MaterialTheme.typography.body1
            )
            Text(
                text = previous.toString(),
                modifier = Modifier.weight(0.25f),
                style = MaterialTheme.typography.body1
            )
        }
    }
}
