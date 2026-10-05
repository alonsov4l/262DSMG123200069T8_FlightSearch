package com.example.flightsearch.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "layout_preferences")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val SEARCH_QUERY = stringPreferencesKey("search_query")
    }

    val searchQuery: Flow<String> = dataStore.data.map { preferences ->
        preferences[SEARCH_QUERY] ?: ""
    }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY] = query
        }
    }
}