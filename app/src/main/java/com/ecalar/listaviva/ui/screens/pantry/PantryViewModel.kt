package com.ecalar.listaviva.ui.screens.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.catalog.allProducts
import com.ecalar.listaviva.data.catalog.catalogData
import com.ecalar.listaviva.data.local.LocalPreferences
import com.ecalar.listaviva.data.repository.AuthRepository
import com.ecalar.listaviva.data.repository.PantryRepository
import com.ecalar.listaviva.domain.model.PantryItem
import com.ecalar.listaviva.domain.model.ProductStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PantryState(
    val items: List<PantryItem> = emptyList(),
    val filteredItems: List<PantryItem> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: PantryItem? = null,
    val showStatusDialog: PantryItem? = null
)

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryRepository: PantryRepository,
    private val localPreferences: LocalPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PantryState())
    val state: StateFlow<PantryState> = _state.asStateFlow()

    private val familyId: String?
        get() = runBlocking { localPreferences.familyId.first() }

    init {
        loadPantryItems()
    }

    private fun loadPantryItems() {
        val fid = familyId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            pantryRepository.getPantryItems(fid)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
                .collect { items ->
                    _state.value = _state.value.copy(
                        items = items,
                        isLoading = false,
                        error = null
                    )
                    applyFilters()
                }
        }
    }

    fun selectCategory(category: String?) {
        _state.value = _state.value.copy(selectedCategory = category)
        applyFilters()
    }

    fun onSearchQueryChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _state.value.items

        // Filtrar por categoría
        _state.value.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filtrar por búsqueda
        if (_state.value.searchQuery.isNotBlank()) {
            val query = _state.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                it.category.lowercase().contains(query) ||
                it.subcategory.lowercase().contains(query)
            }
        }

        _state.value = _state.value.copy(filteredItems = filtered)
    }

    fun updateItemStatus(item: PantryItem, newStatus: ProductStatus) {
        val fid = familyId ?: return
        viewModelScope.launch {
            pantryRepository.updateItemStatus(fid, item.id, newStatus)
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = e.localizedMessage
                    )
                }
        }
    }

    fun addItem(
        name: String,
        category: String,
        subcategory: String,
        format: String,
        notes: String
    ) {
        val fid = familyId ?: return
        val uid = authRepository.getCurrentUser()?.uid ?: return

        val item = PantryItem(
            name = name,
            category = category,
            subcategory = subcategory,
            format = format,
            notes = notes,
            status = ProductStatus.COMPLETO,
            addedBy = uid
        )

        viewModelScope.launch {
            pantryRepository.addItem(fid, item)
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = e.localizedMessage
                    )
                }
        }
    }

    fun deleteItem(item: PantryItem) {
        val fid = familyId ?: return
        viewModelScope.launch {
            pantryRepository.deleteItem(fid, item.id)
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        error = e.localizedMessage
                    )
                }
        }
    }

    fun showDeleteDialog(item: PantryItem?) {
        _state.value = _state.value.copy(showDeleteDialog = item)
    }

    fun showStatusDialog(item: PantryItem?) {
        _state.value = _state.value.copy(showStatusDialog = item)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    // Datos del catálogo para los dropdowns
    fun getCategories(): List<String> = catalogData.map { it.name }

    fun getSubcategories(category: String): List<String> {
        return catalogData.find { it.name == category }?.subcategories?.map { it.name } ?: emptyList()
    }

    fun getCatalogProducts(): List<String> = allProducts
}

private fun <T> runBlocking(block: suspend () -> T): T {
    return kotlinx.coroutines.runBlocking { block() }
}

// Método para añadir a lista de compra desde despensa
fun addToShoppingList(item: PantryItem, listId: String) {
    // Se llamará desde la UI cuando se seleccione "Agotado" y una lista
}
