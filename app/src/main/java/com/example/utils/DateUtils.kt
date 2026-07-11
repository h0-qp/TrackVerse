package com.example.utils

import java.time.LocalDate
import java.time.format.DateTimeParseException

object DateUtils {
    fun adjustAirDateOffset(dateStr: String?): String? {
        if (dateStr.isNullOrEmpty()) return dateStr
        return try {
            LocalDate.parse(dateStr).plusDays(1).toString()
        } catch (e: Exception) {
            dateStr
        }
    }
}
