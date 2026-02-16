package com.example.internetapi.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.internetapi.R
import com.example.internetapi.api.Resource
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.MediaViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MediaScreen(
                        viewModel = viewModel,
                        onOpenDetails = { item ->
                            Intent(this, MediaDetailsActivity::class.java).apply {
                                putExtra("name", item.name)
                                putExtra("mediaId", item.id)
                            }.let {
                                startActivity(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaScreen(
    viewModel: MediaViewModel,
    onOpenDetails: (MediaType) -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val failedLoadMediaTypesMessage = stringResource(id = R.string.error_failed_load_media_types_data)
    val failedAddNewMediaMessage = stringResource(id = R.string.error_failed_add_new_media)
    val emptyStateNoDataMessage = stringResource(id = R.string.empty_state_no_data)
    val addNewMediaTypeTitle = stringResource(id = R.string.dialog_title_add_new_media_type)

    var refreshKey by remember { mutableStateOf(0) }
    val mediaTypesLiveData = remember(refreshKey) { viewModel.getMediaTypes() }
    var mediaTypesResource by remember { mutableStateOf<Resource<List<MediaType>>?>(null) }
    DisposableEffect(mediaTypesLiveData, lifecycleOwner) {
        val observer = Observer<Resource<List<MediaType>>> { mediaTypesResource = it }
        mediaTypesLiveData.observe(lifecycleOwner, observer)
        onDispose { mediaTypesLiveData.removeObserver(observer) }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newMediaName by remember { mutableStateOf("") }
    var addRequestName by remember { mutableStateOf<String?>(null) }
    val addLiveData = remember(addRequestName) {
        addRequestName?.let { viewModel.addNewMediaType(MediaTypeRequest(it)) }
    }
    var addResource by remember { mutableStateOf<Resource<MediaType>?>(null) }
    DisposableEffect(addLiveData, lifecycleOwner) {
        val liveData = addLiveData
        if (liveData == null) {
            addResource = null
            onDispose { }
        } else {
            val observer = Observer<Resource<MediaType>> { addResource = it }
            liveData.observe(lifecycleOwner, observer)
            onDispose { liveData.removeObserver(observer) }
        }
    }

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    LaunchedEffect(mediaTypesResource?.status) {
        if (mediaTypesResource?.status == Status.ERROR) {
            showMessage(failedLoadMediaTypesMessage)
        }
    }

    LaunchedEffect(addResource?.status) {
        when (addResource?.status) {
            Status.SUCCESS -> {
                addResource?.data?.let { added ->
                    showMessage("Dodano ${added.name}")
                }
                addRequestName = null
                newMediaName = ""
                refreshKey += 1
            }
            Status.ERROR -> {
                val name = addRequestName
                if (!name.isNullOrBlank()) {
                    showMessage(context.getString(R.string.error_failed_add_new_media_with_name, name))
                } else {
                    showMessage(failedAddNewMediaMessage)
                }
                addRequestName = null
            }
            else -> Unit
        }
    }

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
            val items = mediaTypesResource?.data ?: emptyList()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.title_activity_media),
                        style = MaterialTheme.typography.h5
                    )
                }

                if (items.isEmpty() && mediaTypesResource?.status == Status.SUCCESS) {
                    item {
                        Text(
                            text = emptyStateNoDataMessage,
                            style = MaterialTheme.typography.body1
                        )
                    }
                } else {
                    items(items) { mediaType ->
                        MediaTypeCard(mediaType = mediaType, onClick = { onOpenDetails(mediaType) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = addNewMediaTypeTitle) },
            text = {
                TextField(
                    value = newMediaName,
                    onValueChange = { newMediaName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newMediaName.trim()
                        if (trimmed.isNotEmpty()) {
                            addRequestName = trimmed
                        }
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
private fun MediaTypeCard(
    mediaType: MediaType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 6.dp
    ) {
        Text(
            text = mediaType.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.h6
        )
    }
}
