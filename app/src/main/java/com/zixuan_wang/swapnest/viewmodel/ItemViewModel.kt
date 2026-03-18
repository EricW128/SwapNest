package com.zixuan_wang.swapnest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.zixuan_wang.swapnest.model.Item
import com.zixuan_wang.swapnest.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.flow.*

sealed interface AddItemUiState {
    data object Idle : AddItemUiState
    data object Uploading : AddItemUiState
    data object Success : AddItemUiState
    data class Error(val message: String) : AddItemUiState
}

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val allItems: StateFlow<List<Item>> = _items
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("全部")
    
    val items: StateFlow<List<Item>> = combine(_items, _searchQuery, _selectedCategory) { items, query, category ->
        items.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) || 
                             item.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "全部" || item.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _requestedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val requestedItemIds: StateFlow<Set<String>> = _requestedItemIds

    private val _claimedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val claimedItemIds: StateFlow<Set<String>> = _claimedItemIds

    private val _favoritedItemIds = MutableStateFlow<Set<String>>(emptySet())
    val favoritedItemIds: StateFlow<Set<String>> = _favoritedItemIds

    private val _addItemUiState = MutableStateFlow<AddItemUiState>(AddItemUiState.Idle)
    val addItemUiState: StateFlow<AddItemUiState> = _addItemUiState

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userRegistrations: List<ListenerRegistration> = emptyList()
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val uid = firebaseAuth.currentUser?.uid
        attachUserActionListeners(uid)
    }

    init {
        fetchItems()
        auth.addAuthStateListener(authListener)
        attachUserActionListeners(auth.currentUser?.uid)
    }

    private fun attachUserActionListeners(uid: String?) {
        userRegistrations.forEach { it.remove() }
        userRegistrations = emptyList()

        if (uid.isNullOrBlank()) {
            _requestedItemIds.value = emptySet()
            _claimedItemIds.value = emptySet()
            _favoritedItemIds.value = emptySet()
            return
        }

        fun listenIds(sub: String, target: MutableStateFlow<Set<String>>): ListenerRegistration {
            return firestore.collection("users").document(uid).collection(sub)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) return@addSnapshotListener
                    target.value = snapshot.documents.map { it.id }.toSet()
                }
        }

        userRegistrations = listOf(
            listenIds("requests", _requestedItemIds),
            listenIds("claims", _claimedItemIds),
            listenIds("favorites", _favoritedItemIds)
        )
    }

    private fun fetchItems() {
        viewModelScope.launch {
            repository.getItems()
                .catch { }
                .collect { _items.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChanged(category: String) {
        _selectedCategory.value = category
    }

    fun requestItem(id: String) {
        val uid = auth.currentUser?.uid ?: return
        _requestedItemIds.value += id
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid).collection("requests").document(id).set(
                    mapOf(
                        "itemId" to id,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()
            }.onFailure {
                _requestedItemIds.value -= id
            }
        }
    }

    fun isRequested(id: String): Boolean {
        return _requestedItemIds.value.contains(id)
    }

    fun getRequestedItems(): List<Item> {
        return _items.value.filter { _requestedItemIds.value.contains(it.id) }
    }

    fun claimItem(id: String) {
        val uid = auth.currentUser?.uid ?: return
        _claimedItemIds.value += id
        viewModelScope.launch {
            runCatching {
                firestore.collection("users").document(uid).collection("claims").document(id).set(
                    mapOf(
                        "itemId" to id,
                        "timestamp" to System.currentTimeMillis()
                    )
                ).await()
            }.onFailure {
                _claimedItemIds.value -= id
            }
        }
    }

    fun isClaimed(id: String): Boolean {
        return _claimedItemIds.value.contains(id)
    }

    fun toggleFavorite(id: String) {
        val uid = auth.currentUser?.uid ?: return
        val currently = _favoritedItemIds.value.contains(id)
        _favoritedItemIds.value = if (currently) _favoritedItemIds.value - id else _favoritedItemIds.value + id

        viewModelScope.launch {
            runCatching {
                val doc = firestore.collection("users").document(uid).collection("favorites").document(id)
                if (currently) {
                    doc.delete().await()
                } else {
                    doc.set(
                        mapOf(
                            "itemId" to id,
                            "timestamp" to System.currentTimeMillis()
                        )
                    ).await()
                }
            }.onFailure {
                _favoritedItemIds.value =
                    if (currently) _favoritedItemIds.value + id else _favoritedItemIds.value - id
            }
        }
    }

    fun isFavorited(id: String): Boolean {
        return _favoritedItemIds.value.contains(id)
    }

    fun addItem(item: Item, imageUri: Uri?) {
        viewModelScope.launch {
            _addItemUiState.value = AddItemUiState.Uploading
            runCatching {
                repository.addItem(item, imageUri)
            }.onSuccess {
                _addItemUiState.value = AddItemUiState.Success
            }.onFailure { t ->
                _addItemUiState.value =
                    AddItemUiState.Error(t.message ?: "发布失败，请稍后重试")
            }
        }
    }

    fun getItem(id: String): Item? {
        return _items.value.find { it.id == id }
    }

    fun resetAddItemUiState() {
        _addItemUiState.value = AddItemUiState.Idle
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
        userRegistrations.forEach { it.remove() }
        userRegistrations = emptyList()
        super.onCleared()
    }
}
