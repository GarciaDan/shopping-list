package org.danigarcia.apps.simple_shopping_list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import org.danigarcia.apps.simple_shopping_list.data.AppSettings

/**
 * Diálogo de alta de item: TextField + confirmar/cancelar.
 */
@Composable
fun AddItemDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Al abrir el diálogo, foco en el campo y teclado visible
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo artículo") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank()
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Pantalla de ajustes (diálogo): endpoint de Firebase, API key/secret
 * y nombre de usuario (se asigna al campo owner).
 */
@Composable
fun SettingsDialog(
    current: AppSettings,
    onSave: (endpoint: String, auth: String, username: String) -> Unit,
    onDismiss: () -> Unit
) {
    var endpoint by remember { mutableStateOf(current.endpoint) }
    var auth by remember { mutableStateOf(current.auth) }
    var username by remember { mutableStateOf(current.username) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuración") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = endpoint,
                    onValueChange = { endpoint = it },
                    label = { Text("Firebase endpoint (URL)") },
                    placeholder = { Text("https://xxx.firebaseio.com/items") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = auth,
                    onValueChange = { auth = it },
                    label = { Text("API key / secret (?auth=)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username (owner)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "El username se guardará como owner en cada artículo creado.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(endpoint, auth, username) },
                enabled = endpoint.isNotBlank() && username.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
