package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightDao
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

class FlightViewModel(
    private val flightDao: FlightDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val favorites = flightDao.getFavorites()

    init {
        viewModelScope.launch {
            userPreferencesRepository.searchQuery.collect { savedQuery ->
                _searchQuery.value = savedQuery
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun searchAirports(query: String) = flightDao.searchAirports(query)

    fun getDestinations(departureCode: String) = flightDao.getAllDestinations(departureCode)

    fun addFavorite(departure: String, destination: String) {
        viewModelScope.launch(Dispatchers.IO) {
            flightDao.insertFavorite(Favorite(departureCode = departure, destinationCode = destination))
        }
    }
}