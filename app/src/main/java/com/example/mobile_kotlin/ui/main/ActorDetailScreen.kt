package com.example.mobile_kotlin.ui.main

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.mobile_kotlin.data.model.Country
import com.example.mobile_kotlin.ui.components.FullScreenLoader
import com.example.mobile_kotlin.ui.components.ImageSlider
import com.example.mobile_kotlin.ui.components.NavBar
import com.example.mobile_kotlin.ui.components.ReviewDialog
import com.example.mobile_kotlin.ui.utils.ErrorMessage
import com.example.mobile_kotlin.ui.utils.UiState
import com.example.mobile_kotlin.ui.utils.toFormattedDate
import com.google.firebase.Timestamp
import android.os.Build
import com.example.mobile_kotlin.data.model.Actor
import com.example.mobile_kotlin.data.model.Review
import com.example.mobile_kotlin.viewmodels.ActorsViewModel
import com.example.mobile_kotlin.viewmodels.AuthViewModel
import com.example.mobile_kotlin.viewmodels.ReviewsViewModel
import org.tensorflow.lite.support.label.Category


@Composable
fun ActorDetailScreen(
    actorId: String?,
    userId: String?,
    navController: NavController
) {
    val actorsViewModel: ActorsViewModel = hiltViewModel()
    val reviewsViewModel: ReviewsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    val actorState by actorsViewModel.detailState.collectAsState()
    val reviewsState by reviewsViewModel.reviewsState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val countries by actorsViewModel.availableFilters.collectAsState()

    var showReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actorId) {
        actorId?.let {
            actorsViewModel.loadActorDetails(it)
            reviewsViewModel.loadReviews(it)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isLoggedIn) {
                FloatingActionButton(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Добавить отзыв")
                }
            }
        },
        bottomBar = { NavBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (actorState) {
                is UiState.Loading -> FullScreenLoader()
                is UiState.Success -> {
                    val actor = (actorState as UiState.Success<Actor>).data
                    ActorDetailContent(
                        actor = actor,
                        countries = countries.countries,
                        reviewsState = reviewsState,
                        userId = userId
                    )
                }
                is UiState.Error -> ErrorMessage((actorState as UiState.Error).message)
                UiState.Empty -> Text("Актёр не найден")
            }
        }
    }

    if (showReviewDialog && actorId != null) {
        ReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { text, rating ->
                reviewsViewModel.addReview(actorId, text, rating)
            }
        )
    }
}

@Composable
private fun SortSelector() {
    val viewModel: ReviewsViewModel = hiltViewModel()

    var expanded by remember { mutableStateOf(false) }
    val currentSort = viewModel.sortType.collectAsState()

    Box(modifier = Modifier.wrapContentSize()) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = "Сортировка"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ReviewsViewModel.SortType.values().forEach { sortType ->
                DropdownMenuItem(
                    text = { Text(sortType.displayName) },
                    onClick = {
                        viewModel.updateSort(sortType)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ActorDetailContent(
    actor: Actor,
    countries: List<Country>,
    reviewsState: UiState<List<Review>>,
    userId: String?
) {
    val reviewsViewModel: ReviewsViewModel = hiltViewModel()

    val countryName = remember(actor.countryId) {
        countries.find { it.id == actor.countryId }?.name ?: "Не указано"
    }

    Column {
        ImageSlider(images = actor.images)

        Column(modifier = Modifier.padding(16.dp)) {
            Text(actor.name, style = MaterialTheme.typography.headlineLarge)
            StarRating(rating = actor.rating)
            Text(actor.description, modifier = Modifier.padding(vertical = 8.dp))

            InfoRow("Дата рождения", actor.birthDate.toFormattedDate())
            InfoRow("Страна", countryName)
            InfoRow("Фильмы", actor.movies.joinToString(", "))
        }

        ReviewsSection(
            reviewsState = reviewsState,
            userId = userId
        )
    }
}

@Composable
private fun StarRating(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ReviewsSection(
    reviewsState: UiState<List<Review>>,
    userId: String?
) {
    val viewModel: ReviewsViewModel = hiltViewModel()

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отзывы", style = MaterialTheme.typography.titleLarge)
            SortSelector()
        }

        when (reviewsState) {
            is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is UiState.Success -> ReviewList(
                reviews = reviewsState.data,
                userId = userId,
                onEdit = { viewModel.updateReview(it.id, it.text, it.rating) },
                onDelete = { viewModel.deleteReview(it.id) }
            )
            is UiState.Error -> Text("Ошибка загрузки: ${reviewsState.message}")
            UiState.Empty -> Text("Пока нет отзывов", modifier = Modifier.padding(16.dp))
        }
    }
}

// Форма редактирования отзыва
@Composable
private fun EditReviewForm(
    initialText: String,
    initialRating: Float,
    onSave: (String, Float) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var rating by remember { mutableStateOf(initialRating) }

    Column {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Текст отзыва") },
            modifier = Modifier.fillMaxWidth()
        )

        StarRatingSelector(
            rating = rating,
            onRatingChange = { rating = it },
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onCancel) {
                Text("Отмена")
            }
            Button(
                onClick = { onSave(text, rating) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
private fun StarRatingSelector(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Рейтинг:", style = MaterialTheme.typography.bodyMedium)
        Row {
            repeat(10) { index ->
                val value = (index + 1).toFloat()
                IconButton(
                    onClick = { onRatingChange(value) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (value <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Оценка $value",
                        tint = if (value <= rating) Color(0xFFFFD700) else Color.Gray
                    )
                }
            }
        }
        Text("${"%.1f".format(rating)}/10", style = MaterialTheme.typography.bodySmall)
    }
}



@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Float) -> Unit
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить отзыв") },
        text = {
            Column {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Текст отзыва") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )

                StarRatingSelector(
                    rating = rating,
                    onRatingChange = { rating = it },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        reviewText.isBlank() -> errorMessage = "Введите текст отзыва"
                        rating < 0.5f -> errorMessage = "Укажите рейтинг"
                        else -> onSubmit(reviewText, rating)
                    }
                }
            ) {
                Text("Отправить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

// Список отзывов
@Composable
private fun ReviewList(
    reviews: List<Review>,
    userId: String?,
    onEdit: (Review) -> Unit,
    onDelete: (Review) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(reviews) { review ->
            ReviewItem(
                review = review,
                userId = userId,
                onEdit = { onEdit(review) },
                onDelete = { onDelete(review) }
            )
        }
    }
}

// Элемент отзыва
@Composable
private fun ReviewItem(
    review: Review,
    userId: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                EditReviewForm(
                    initialText = review.text,
                    initialRating = review.rating,
                    onSave = { text, rating ->
                        onEdit.invoke()
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StarRating(rating = review.rating)
                    Text(
                        text = review.timestamp.toFormattedDate(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = review.text,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                if(review.userId == userId) {
                    Row(modifier = Modifier.align(Alignment.End)) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Удалить", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
