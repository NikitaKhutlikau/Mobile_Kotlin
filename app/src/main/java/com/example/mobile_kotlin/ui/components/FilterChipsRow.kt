package com.example.mobile_kotlin.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile_kotlin.data.model.Award
import com.example.mobile_kotlin.data.model.Country
import com.example.mobile_kotlin.data.model.Genre
import com.example.mobile_kotlin.viewmodels.Filters
import com.example.mobile_kotlin.viewmodels.FiltersData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipsRow(
    filtersData: FiltersData,
    filters: Filters,
    onGenresChange: (Set<String>) -> Unit,
    onCountriesChange: (Set<String>) -> Unit,
    onAwardsChange: (Set<String>) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Жанры
        Column {
            Text(
                text = "Жанры",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                filtersData.genres.forEach { genre: Genre ->
                    FilterChip(
                        selected = filters.selectedGenres.contains(genre.id),
                        onClick = {
                            val newSet = filters.selectedGenres.toMutableSet().apply {
                                if (contains(genre.id)) remove(genre.id) else add(genre.id)
                            }
                            onGenresChange(newSet)
                        },
                        label = { Text(genre.name) }
                    )
                }
            }
        }
        // Страны
        Column {
            Text(
                text = "Страны",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                filtersData.countries.forEach { country: Country ->
                    FilterChip(
                        selected = filters.selectedCountries.contains(country.id),
                        onClick = {
                            val newSet = filters.selectedCountries.toMutableSet().apply {
                                if (contains(country.id)) remove(country.id) else add(country.id)
                            }
                            onCountriesChange(newSet)
                        },
                        label = { Text(country.name) }
                    )
                }
            }
        }
        // Награды
        Column {
            Text(
                text = "Награды",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                filtersData.awards.forEach { award: Award ->
                    FilterChip(
                        selected = filters.selectedAwards.contains(award.id),
                        onClick = {
                            val newSet = filters.selectedAwards.toMutableSet().apply {
                                if (contains(award.id)) remove(award.id) else add(award.id)
                            }
                            onAwardsChange(newSet)
                        },
                        label = { Text(award.name) }
                    )
                }
            }
        }
    }
}