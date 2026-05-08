package com.zozi.helparticlesapp.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private const val DATE_FORMAT = "MMM d, yyyy"

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            .format(Date(timestamp))
    }
}

