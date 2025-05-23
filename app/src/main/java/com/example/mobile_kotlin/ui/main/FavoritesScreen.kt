package com.example.mobile_kotlin.ui.main

import com.example.mobile_kotlin.ui.components.FilterChipsRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.mobile_kotlin.ui.components.FullScreenLoader
import com.example.mobile_kotlin.ui.components.NavBar
import com.example.mobile_kotlin.ui.components.SearchBar
import com.example.mobile_kotlin.ui.utils.ErrorMessage
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.viewmodels.AuthViewModel
import com.example.mobile_kotlin.viewmodels.FavoritesViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(navController: NavController) {
    val viewModel: FavoritesViewModel = hiltViewModel()
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
                UiState.Empty -> EmptyFavorites()
                is UiState.Error -> EmptyMessage((listState as UiState.Error).message)
                is UiState.Success -> {
                    val actors = (listState as UiState.Success<List<Actor>>).data
                    if (actors.isEmpty()) {
                        EmptyFavorites()
                    } else {
                        val actorUis = actors.map { actor ->
                            val countryName = filtersData.countries.find { it.id == actor.countryId }?.name ?: ""
                            ActorUi(actor = actor, isFavorite = true, countryName = countryName)
                        }
                        ActorsList(
                            actors = actorUis,
                            onFavoriteClick = { actorId ->
                                coroutineScope.launch { viewModel.toggleFavorite(actorId) }
                            },
                            onItemClick = { actorId -> navController.navigate("actor_detail/$actorId") },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp)
        )
        Text("Нет избранных актёров", style = MaterialTheme.typography.titleMedium)
    }
}