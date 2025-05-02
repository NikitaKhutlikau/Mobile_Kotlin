package com.example.mobile_kotlin.ui.utils

import android.os.Build
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

fun Timestamp.toFormattedDate(): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        .format(this.toDate())
}