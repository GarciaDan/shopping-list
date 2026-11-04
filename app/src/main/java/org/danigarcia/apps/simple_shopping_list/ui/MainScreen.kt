package org.danigarcia.apps.simple_shopping_list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.danigarcia.apps.simple_shopping_list.MainViewModel
import org.danigarcia.apps.simple_shopping_list.UiState
import org.danigarcia.apps.simple_shopping_list.model.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val state: UiState by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de la compra") },
                actions = {
                    IconButton(onClick = { vm.showSettings(true) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.showAddDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir artículo")
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.items.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { vm.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (state.items.isEmpty()) {
                        Text(
                            "La lista está vacía.\nPulsa + para añadir artículos.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                SwipeItem(
                                    item = item,
                                    onToggle = { vm.toggleChecked(item) },
                                    onDelete = { vm.deleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddItemDialog(
            onConfirm = { description ->
                vm.showAddDialog(false)
                vm.addItem(description)
            },
            onDismiss = { vm.showAddDialog(false) }
        )
    }

    if (state.showSettings) {
        SettingsDialog(
            current = state.settings,
            onSave = vm::saveSettings,
            onDismiss = { vm.showSettings(false) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeItem(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.5f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // Fondo rojo con icono de papelera visible al deslizar a la izquierda
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .clickable { onToggle() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() }
            )
            Column(Modifier.padding(end = 12.dp)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = formatCreated(item),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatCreated(item: ShoppingItem): String {
    // Línea secundaria (fuente pequeña): fecha de creación y quién lo añadió
    val owner = item.owner.ifBlank { "—" }
    return "${item.created} · $owner"
}
