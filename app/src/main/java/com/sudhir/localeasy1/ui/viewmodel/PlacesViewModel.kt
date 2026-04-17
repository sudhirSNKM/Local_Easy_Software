package com.sudhir.localeasy1.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sudhir.localeasy1.data.Place
import com.sudhir.localeasy1.data.samplePlaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class PlacesViewModel : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    val filteredPlaces = _searchQuery.map { query ->
        if (query.isEmpty()) {
            samplePlaces
        } else {
            samplePlaces.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
    }

    val favoritePlaces = _favoriteIds.map { ids ->
        samplePlaces.filter { it.id in ids }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(placeId: String) {
        _favoriteIds.value = if (_favoriteIds.value.contains(placeId)) {
            _favoriteIds.value - placeId
        } else {
            _favoriteIds.value + placeId
        }
    }
}
