package com.example.mobile_kotlin.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.model.Award
import com.example.mobile_kotlin.data.model.Country
import com.example.mobile_kotlin.data.model.Genre
import com.example.mobile_kotlin.data.repository.ActorsRepository
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.ui.utils.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


// Набор доступных фильтров для UI
data class FiltersData(
    val genres: List<Genre> = emptyList(),
    val countries: List<Country> = emptyList(),
    val awards: List<Award> = emptyList()
)

// Состояние выбранных фильтров и поисковой строки
data class Filters(
    val query: String = "",
    val selectedGenres: Set<String> = emptySet(),   // id жанров
    val selectedCountries: Set<String> = emptySet(), // id стран
    val selectedAwards: Set<String> = emptySet()     // id наград
)

abstract class BaseActorViewModel(
    protected val actorsRepo: ActorsRepository,
    protected val authRepo: AuthRepository
) : ViewModel() {

    // Состояния
    protected val _listState = MutableStateFlow<UiState<List<Actor>>>(UiState.Loading)
    val listState: StateFlow<UiState<List<Actor>>> = _listState.asStateFlow()

    protected val _detailState = MutableStateFlow<UiState<Actor>>(UiState.Loading)
    val detailState: StateFlow<UiState<Actor>> = _detailState.asStateFlow()

    // Фильтры
    protected val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters.asStateFlow()

    protected val _availableFilters = MutableStateFlow(FiltersData())
    val availableFilters: StateFlow<FiltersData> = _availableFilters.asStateFlow()

    // Источник данных (абстрактный)
    protected abstract val sourceData: StateFlow <List<Actor>>

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        loadAvailableFilters()
        //loadActors() // Инициализируем первую загрузку
    }

    // Основной метод загрузки актеров
    fun loadActors() {
        viewModelScope.launch {
            try {
                _listState.value = UiState.Loading
                val actors = sourceData.first()
                _listState.value = UiState.Success(applyFilters(actors, filters.value))
            } catch (e: Exception) {
                _listState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun refreshData() {
        this.loadActors()
        this.loadAvailableFilters()
    }

    protected fun setupActorsStream() {
        viewModelScope.launch {
            combine(sourceData, _filters) { actors, filters ->
                applyFilters(actors, filters)
            }.catch { e ->
                _listState.value = UiState.Error("Ошибка потока: ${e.message}")
            }.collect { filteredActors ->
                _listState.value = when {
                    filteredActors.isEmpty() -> UiState.Empty
                    else -> UiState.Success(filteredActors)
                }
            }
        }
    }

    fun loadActorDetails(actorId: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val actor = actorsRepo.getActorDetails(actorId)
                _detailState.value = actor?.let { UiState.Success(it) }
                    ?: UiState.Error("Актер не найден")
            } catch (e: Exception) {
                _detailState.value = UiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }
    fun applyFilters(
        query: String = _filters.value.query,
        genres: Set<String> = _filters.value.selectedGenres,
        countries: Set<String> = _filters.value.selectedCountries,
        awards: Set<String> = _filters.value.selectedAwards
    ) {
        _filters.value = Filters(query, genres, countries, awards)
    }

    fun search(query: String) {
        applyFilters(query = query)
    }

    suspend fun toggleFavorite(actorId: String) {
        authRepo.getCurrentUser().first()?.id?.let { userId ->
            actorsRepo.toggleFavorite(actorId)
            if (this is FavoritesViewModel) loadActors()
        }
    }

    private fun loadAvailableFilters() {
        viewModelScope.launch {
            combine(
                actorsRepo.getGenres(),
                actorsRepo.getCountries(),
                actorsRepo.getAwards()
            ) { genres, countries, awards ->
                FiltersData(genres, countries, awards)
            }.catch { e ->
            _listState.value = UiState.Error("Ошибка фильтров: ${e.message}")
            }.collect {
                _availableFilters.value = it
            }
        }
    }

    private fun applyFilters(actors: List<Actor>, filters: Filters): List<Actor> {
        return actors.filter { actor ->
            actor.name.contains(filters.query, true) &&
                    (filters.selectedGenres.isEmpty() || actor.genreIds.any { it in filters.selectedGenres }) &&
                    (filters.selectedCountries.isEmpty() || actor.countryId in filters.selectedCountries) &&
                    (filters.selectedAwards.isEmpty() || actor.awardIds.any { it in filters.selectedAwards })
        }
    }
}