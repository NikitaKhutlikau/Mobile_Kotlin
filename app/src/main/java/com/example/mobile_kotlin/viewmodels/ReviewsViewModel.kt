package com.example.mobile_kotlin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_kotlin.data.model.Review
import com.example.mobile_kotlin.data.repository.AuthRepository
import com.example.mobile_kotlin.data.repository.ReviewsRepository
import com.example.mobile_kotlin.ui.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val reviewsRepo: ReviewsRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState

    private val _sortType = MutableStateFlow(SortType.DATE_DESC)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()


    // Обновляем метод загрузки отзывов
    fun loadReviews(actorId: String) {
        viewModelScope.launch {
            _reviewsState.value = UiState.Loading
            try {
                reviewsRepo.observeReviews(actorId)
                    .combine(sortType) { reviews, sort ->
                        reviews.sortedWith(sort.comparator)
                    }
                    .collect { sorted ->
                        _reviewsState.value = UiState.Success(sorted)
                    }
            } catch (e: Exception) {
                _reviewsState.value = UiState.Error(e.message ?: "Ошибка сортировки")
            }
        }
    }

    fun addReview(actorId: String, text: String, rating: Float) {
        viewModelScope.launch {
            try {
                val userId = authRepo.getCurrentUser().first()?.id ?: throw Exception("Не авторизован")
                reviewsRepo.addReview(
                    Review(
                        userId = userId,
                        actorId = actorId,
                        text = text,
                        rating = rating.coerceIn(0f, 10f)
                    )
                )
            } catch (e: Exception) {
                _reviewsState.value = UiState.Error("Ошибка: ${e.message}")
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                reviewsRepo.deleteReview(reviewId)
            } catch (e: Exception) {
                _reviewsState.value = UiState.Error("Ошибка удаления: ${e.message}")
            }
        }
    }

    fun updateReview(reviewId: String, newText: String, newRating: Float) {
        viewModelScope.launch {
            try {
                reviewsRepo.updateReview(reviewId, newText, newRating.coerceIn(0f, 10f))
            } catch (e: Exception) {
                _reviewsState.value = UiState.Error("Ошибка обновления: ${e.message}")
            }
        }
    }

    /*private fun applySorting(reviews: List<Review>) = when (_sortType.value) {
        SortType.DATE_ASC -> reviews.sortedBy { it.timestamp }
        SortType.DATE_DESC -> reviews.sortedByDescending { it.timestamp }
        SortType.RATING_ASC -> reviews.sortedBy { it.rating }
        SortType.RATING_DESC -> reviews.sortedByDescending { it.rating }
    }*/

    fun updateSort(newType: SortType) {
        _sortType.value = newType
    }

    enum class SortType(
        val displayName: String, // Добавляем поле для отображаемого имени
        val comparator: Comparator<Review>
    ) {
        DATE_ASC(
            "По дате (старые)",
            compareBy { it.timestamp }
        ),
        DATE_DESC(
            "По дате (новые)",
            compareByDescending { it.timestamp }
        ),
        RATING_ASC(
            "По рейтингу (↑)",
            compareBy { it.rating }
        ),
        RATING_DESC(
            "По рейтингу (↓)",
            compareByDescending { it.rating }
        );
    }
}