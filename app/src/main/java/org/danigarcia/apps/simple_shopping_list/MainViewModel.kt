package org.danigarcia.apps.simple_shopping_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.danigarcia.apps.simple_shopping_list.data.AppSettings
import org.danigarcia.apps.simple_shopping_list.data.SettingsRepository
import org.danigarcia.apps.simple_shopping_list.data.ShoppingListRepository
import org.danigarcia.apps.simple_shopping_list.data.sortedForDisplay
import org.danigarcia.apps.simple_shopping_list.model.ShoppingItem

data class UiState(
    val items: List<ShoppingItem> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val showSettings: Boolean = false,
    val showAddDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val listRepo = ShoppingListRepository()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        // Observa los ajustes persistidos y carga la lista al arrancar.
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                val isFirstLoad = _state.value.settings.endpoint.isBlank()
                _state.value = _state.value.copy(settings = settings)
                if (isFirstLoad) refresh(initial = true)
            }
        }
    }

    fun showSettings(show: Boolean) {
        _state.value = _state.value.copy(showSettings = show)
    }

    fun showAddDialog(show: Boolean) {
        _state.value = _state.value.copy(showAddDialog = show)
    }

    fun saveSettings(endpoint: String, auth: String, username: String) {
        viewModelScope.launch {
            settingsRepo.save(AppSettings(endpoint, auth, username))
            _state.value = _state.value.copy(showSettings = false)
            refresh(initial = true)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Pull-to-refresh: recarga el JSON completo desde el servidor. */
    fun refresh(initial: Boolean = false) {
        val settings = _state.value.settings
        if (!settings.isConfigured) {
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                showSettings = true,
                error = null
            )
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = initial,
                isRefreshing = !initial,
                error = null
            )
            try {
                val items = listRepo.fetchItems(settings)
                _state.value = _state.value.copy(items = items.sortedForDisplay())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Error de red")
            } finally {
                _state.value = _state.value.copy(isLoading = false, isRefreshing = false)
            }
        }
    }

    /**
     * Añadir item: GET -> insertar nuevo objeto (created/updated = ahora,
     * owner = usuario configurado, checked = false) -> PUT del JSON completo.
     */
    fun addItem(description: String) {
        val settings = _state.value.settings
        val desc = description.trim()
        if (desc.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val current = listRepo.fetchItems(settings).toMutableList()
                val now = ShoppingListRepository.nowIso()
                current += ShoppingItem(
                    id = "item-${System.currentTimeMillis()}",
                    created = now,
                    updated = now,
                    description = desc,
                    owner = settings.username,
                    checked = false
                )
                listRepo.pushItems(settings, current)
                _state.value = _state.value.copy(items = current.sortedForDisplay())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Error al añadir")
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Marcar/desmarcar: GET -> actualizar checked. Al marcar, updated = ahora;
     * al desmarcar, updated = created. PUT del JSON completo.
     */
    fun toggleChecked(item: ShoppingItem) {
        val settings = _state.value.settings
        viewModelScope.launch {
            try {
                val current = listRepo.fetchItems(settings).toMutableList()
                val index = current.indexOfFirst { it.id == item.id }
                if (index < 0) return@launch
                val old = current[index]
                val updated = if (!old.checked) {
                    old.copy(checked = true, updated = ShoppingListRepository.nowIso())
                } else {
                    old.copy(checked = false, updated = old.created)
                }
                current[index] = updated
                listRepo.pushItems(settings, current)
                _state.value = _state.value.copy(items = current.sortedForDisplay())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Error al actualizar")
            }
        }
    }

    /** Eliminar (swipe): GET -> filtrar el elemento -> PUT del JSON completo. */
    fun deleteItem(item: ShoppingItem) {
        val settings = _state.value.settings
        viewModelScope.launch {
            try {
                val current = listRepo.fetchItems(settings)
                val remaining = current.filterNot { it.id == item.id }
                listRepo.pushItems(settings, remaining)
                _state.value = _state.value.copy(items = remaining.sortedForDisplay())
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Error al eliminar")
                refresh()
            }
        }
    }
}
