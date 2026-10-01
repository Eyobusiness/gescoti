package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    fun formatFcfa(amount: Double): String {
        val format = NumberFormat.getNumberInstance(Locale.FRENCH)
        format.maximumFractionDigits = 0
        return "${format.format(amount)} F"
    }

    fun formatF(amount: Double): String = formatFcfa(amount)
}

object DateUtils {
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
    private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }
}
