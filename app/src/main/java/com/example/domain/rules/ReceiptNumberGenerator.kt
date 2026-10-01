package com.example.domain.rules

import java.util.Locale

object ReceiptNumberGenerator {
    fun generate(sigle: String = "REC", annee: String = "2026", sequenceNumber: Int): String {
        val cleanSigle = sigle.trim().uppercase(Locale.ROOT).replace(" ", "")
        val cleanYear = if (annee.contains("-")) annee.split("-")[0] else annee
        val formattedSeq = String.format(Locale.ROOT, "%06d", sequenceNumber)
        return "$cleanSigle-$cleanYear-$formattedSeq"
    }
}
