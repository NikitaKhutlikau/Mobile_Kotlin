package com.example.mobile_kotlin.ui.components

import android.widget.RatingBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Float) -> Unit
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить отзыв") },
        text = {
            Column {
                TextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Ваш отзыв") }
                )
                RatingBar(
                    rating = rating,
                    onRatingChange = { rating = it },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(reviewText, rating) }) {
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

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        repeat(5) { index ->
            IconButton(
                onClick = { onRatingChange(index + 1f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Рейтинг ${index + 1}",
                    tint = if (index < rating) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    }
}