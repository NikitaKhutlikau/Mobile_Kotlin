package com.example.mobile_kotlin.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.ui.components.ActorsList
import com.example.mobile_kotlin.ui.components.FilterChipsRow
import com.example.mobile_kotlin.ui.components.FullScreenLoader
import com.example.mobile_kotlin.ui.components.NavBar
import com.example.mobile_kotlin.ui.components.SearchBar
import com.example.mobile_kotlin.ui.navigation.Destinations.ACTOR_DETAIL
import com.example.mobile_kotlin.ui.utils.ErrorMessage
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.viewmodels.ActorsViewModel
import com.example.mobile_kotlin.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


data class ActorUi(
    val actor: Actor,
    val isFavorite: Boolean,
    val countryName: String
)

@Composable
fun ActorsScreen(navController: NavController) {
    val viewModel: ActorsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    val listState by viewModel.listState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val filtersData by viewModel.availableFilters.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            SearchBar(
                query = filters.query,
                onQueryChange = viewModel::search
            )
        },
        bottomBar = { NavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                //.verticalScroll(rememberScrollState())
        ) {
            // Фильтры
            FilterChipsRow(
                filtersData = filtersData,
                filters = filters,
                onGenresChange = { viewModel.applyFilters(genres = it) },
                onCountriesChange = { viewModel.applyFilters(countries = it) },
                onAwardsChange = { viewModel.applyFilters(awards = it) }
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (listState) {
                UiState.Loading -> FullScreenLoader()
                UiState.Empty -> EmptyMessage("Актёры не найдены")
                is UiState.Error -> EmptyMessage((listState as UiState.Error).message)
                is UiState.Success -> {
                    val actors = (listState as UiState.Success<List<Actor>>).data
                    val actorUis = actors.map { actor ->
                        val isFav = currentUser?.favoriteActorIds?.contains(actor.id) == true
                        val countryName = filtersData.countries.find { it.id == actor.countryId }?.name ?: ""
                        ActorUi(actor = actor, isFavorite = isFav, countryName = countryName)
                    }
                    ActorsList(
                        actors = actorUis,
                        onFavoriteClick = { actorId ->
                            coroutineScope.launch { viewModel.toggleFavorite(actorId) }
                        },
                        onItemClick = { actorId -> navController.navigate("$ACTOR_DETAIL/$actorId") },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyMessage(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Info, contentDescription = null)
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}