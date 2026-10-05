package com.example.flightsearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.FlightDatabase
import com.example.flightsearch.data.UserPreferencesRepository
import com.example.flightsearch.data.dataStore
import com.example.flightsearch.ui.FlightViewModel
import com.example.flightsearch.ui.theme.FlightSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = FlightDatabase.getDatabase(this)
        val preferencesRepository = UserPreferencesRepository(dataStore)

        val factory = viewModelFactory {
            initializer {
                FlightViewModel(database.flightDao(), preferencesRepository)
            }
        }

        setContent {
            FlightSearchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: FlightViewModel = viewModel(factory = factory)
                    FlightApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FlightApp(viewModel: FlightViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())

    var selectedAirport: Airport? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                viewModel.updateSearchQuery(it)
                selectedAirport = null
            },
            label = { Text("Buscar aeropuerto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedAirport != null) {
            val currentAirport = selectedAirport!!
            val destinations by viewModel.getDestinations(currentAirport.iataCode).collectAsState(initial = emptyList())

            Text(
                text = "Vuelos desde ${currentAirport.iataCode}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(destinations) { destination ->
                    FlightRow(
                        departure = currentAirport,
                        destination = destination,
                        onFavoriteClick = {
                            viewModel.addFavorite(currentAirport.iataCode, destination.iataCode)
                        }
                    )
                }
            }

        } else if (searchQuery.isEmpty()) {
            Text(
                text = "Rutas Favoritas",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (favorites.isEmpty()) {
                Text("Aún no tienes rutas favoritas.")
            } else {
                LazyColumn {
                    items(favorites) { favorite ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "${favorite.departureCode} ->${favorite.destinationCode}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

        } else {
            val airports by viewModel.searchAirports(searchQuery).collectAsState(initial = emptyList())

            LazyColumn {
                items(airports) { airport ->
                    AirportSuggestionRow(airport = airport) {
                        selectedAirport = airport
                    }
                }
            }
        }
    }
}

@Composable
fun AirportSuggestionRow(airport: Airport, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(text = airport.iataCode, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = airport.name)
    }
}

@Composable
fun FlightRow(
    departure: Airport,
    destination: Airport,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Salida", style = MaterialTheme.typography.labelSmall)
                Text(text = "${departure.iataCode} -${departure.name}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Llegada", style = MaterialTheme.typography.labelSmall)
                Text(text = "${destination.iataCode} -${destination.name}")
            }

            Button(onClick = onFavoriteClick) {
                Text("Guardar")
            }
        }
    }
}