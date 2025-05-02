package com.example.mobile_kotlin.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile_kotlin.ui.main.ActorUi

@Composable
fun ActorsList(
    actors: List<ActorUi>,
    onFavoriteClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(actors) { item ->
            ActorCard(
                item = item,
                onFavoriteClick = { onFavoriteClick(item.actor.id) },
                onClick = { onItemClick(item.actor.id) }
            )
        }
    }
}