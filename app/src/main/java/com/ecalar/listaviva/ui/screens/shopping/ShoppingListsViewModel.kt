package com.ecalar.listaviva.ui.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecalar.listaviva.data.local.LocalPreferences
import com.ecalar.listaviva.data.repository.AuthRepository
import com.ecalar.listaviva.data.repository.PantryRepository
import com.ecalar.listaviva.data.repository.ShoppingListRepository
import com.ecalar.listaviva.domain.model.ProductStatus
import com.ecalar.listaviva.domain.model.ShoppingItem
import com.ecalar.listaviva.domain.model.ShoppingList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class ShoppingListsState(
    val lists: List<ShoppingList> = emptyList(),
    val selectedListId: String? = null,
    val selectedListName: String = "",
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: ShoppingList? = null,
    val showRenameDialog: ShoppingList? = null,
    val newListName: String = ""
)

@HiltViewModel
class ShoppingListsViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
    private val pantryRepository: PantryRepository,
    private val authRepository: AuthRepository,
    private val localPreferences: LocalPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingListsState())
    val state: StateFlow<ShoppingListsState> = _state.asStateFlow()

    private val familyId: String
        get() = runBlocking { localPreferences.familyId.first() ?: "" }

    init {
        loadLists()
    }

    private fun loadLists() {
        if (familyId.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            shoppingListRepository.getShoppingLists(familyId)
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.localizedMessage
                    )
                }
                .collect { lists ->
                    _state.value = _state.value.copy(
                        lists = lists,
                        isLoading = false
                    )
                    // Si hay listas y no hay una seleccionada, seleccionar la primera
                    if (lists.isNotEmpty() && _state.value.selectedListId == null) {
                        selectList(lists.first())
                    }
                }
        }
    }

    fun selectList(list: ShoppingList) {
        _state.value = _state.value.copy(
            selectedListId = list.id,
            selectedListName = list.name
        )
        loadItems(list.id)
    }

    private fun loadItems(listId: String) {
        if (familyId.isEmpty()) return
        viewModelScope.launch {
            shoppingListRepository.getShoppingItems(familyId, listId)
                .catch { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
                .collect { items ->
                    _state.value = _state.value.copy(items = items)
                }
        }
    }

    fun createList() {
        val name = _state.value.newListName.trim()
        if (name.isEmpty()) return

        val uid = authRepository.getCurrentUser()?.uid ?: return
        val color = listOf(0xFFFF8C42, 0xFF2E8B57, 0xFF4287F5, 0xFFE91E63, 0xFF9C27B0).random()

        viewModelScope.launch {
            shoppingListRepository.createList(familyId, name, color, uid)
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
            _state.value = _state.value.copy(showCreateDialog = false, newListName = "")
        }
    }

    fun renameList() {
        val list = _state.value.showRenameDialog ?: return
        val newName = _state.value.newListName.trim()
        if (newName.isEmpty()) return

        viewModelScope.launch {
            shoppingListRepository.updateListName(familyId, list.id, newName)
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
            _state.value = _state.value.copy(showRenameDialog = null, newListName = "")
        }
    }

    fun deleteList() {
        val list = _state.value.showDeleteDialog ?: return
        viewModelScope.launch {
            shoppingListRepository.deleteList(familyId, list.id)
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
            _state.value = _state.value.copy(
                showDeleteDialog = null,
                selectedListId = null,
                items = emptyList()
            )
        }
    }

    fun addItemToList(name: String, pantryItemId: String?, quantity: String = "") {
        val listId = _state.value.selectedListId ?: return
        val uid = authRepository.getCurrentUser()?.uid ?: return
        val alias = runBlocking { localPreferences.userAlias.first() } ?: "Yo"

        viewModelScope.launch {
            shoppingListRepository.addItem(
                familyId = familyId,
                listId = listId,
                name = name,
                pantryItemId = pantryItemId,
                quantity = quantity,
                addedBy = uid,
                addedByAlias = alias
            ).onFailure { e ->
                _state.value = _state.value.copy(error = e.localizedMessage)
            }
        }
    }

    fun markAsPurchased(item: ShoppingItem) {
        val listId = _state.value.selectedListId ?: return
        viewModelScope.launch {
            shoppingListRepository.markAsPurchased(familyId, listId, item.id)
                .onSuccess {
                    // Si tiene referencia a despensa, restaurar stock
                    item.pantryItemId?.let { pantryId ->
                        pantryRepository.updateItemStatus(familyId, pantryId, ProductStatus.COMPLETO)
                    }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
        }
    }

    fun deleteItem(item: ShoppingItem) {
        val listId = _state.value.selectedListId ?: return
        viewModelScope.launch {
            shoppingListRepository.deleteItem(familyId, listId, item.id)
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.localizedMessage)
                }
        }
    }

    // Diálogos
    fun showCreateDialog() { _state.value = _state.value.copy(showCreateDialog = true, newListName = "") }
    fun hideCreateDialog() { _state.value = _state.value.copy(showCreateDialog = false, newListName = "") }
    fun showDeleteDialog(list: ShoppingList) { _state.value = _state.value.copy(showDeleteDialog = list) }
    fun hideDeleteDialog() { _state.value = _state.value.copy(showDeleteDialog = null) }
    fun showRenameDialog(list: ShoppingList) { _state.value = _state.value.copy(showRenameDialog = list, newListName = list.name) }
    fun hideRenameDialog() { _state.value = _state.value.copy(showRenameDialog = null, newListName = "") }
    fun onNewListNameChange(name: String) { _state.value = _state.value.copy(newListName = name) }
    fun clearError() { _state.value = _state.value.copy(error = null) }
}
