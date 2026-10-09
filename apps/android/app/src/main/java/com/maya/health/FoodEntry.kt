package com.maya.health

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/** Manual input: calories and portion are not verified nutrition measurements. */
data class FoodEntry(
    val id: String,
    val foodName: String,
    val energyKcal: Double,
    val portionGrams: Double?,
    val loggedAtEpochMs: Long,
    val source: String = "USER_ENTERED"
) {
    companion object {
        fun newManual(name: String, kcal: Double, grams: Double?, now: Long = System.currentTimeMillis()) =
            FoodEntry(UUID.randomUUID().toString(), name.trim(), kcal, grams, now)
    }
}

object FoodMath {
    fun parsePositiveDecimal(text: String, max: Double): Double? {
        val normal = text.trim().replace(',', '.')
        if (!Regex("""\d{1,5}(\.\d{1,2})?""").matches(normal)) return null
        return normal.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 && it <= max }
    }

    fun parseCalories(text: String): Double? {
        val normal = text.trim().replace(',', '.')
        if (!Regex("""\d{1,5}(\.\d{1,2})?""").matches(normal)) return null
        return normal.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 && it <= 10000 }
    }

    fun isValid(name: String, kcal: Double?, grams: Double?, gramsInput: String): Boolean =
        name.trim().length in 1..120 &&
            kcal != null && kcal.isFinite() && kcal in 0.0..10000.0 &&
            (gramsInput.isBlank() || (grams != null && grams.isFinite() && grams > 0 && grams <= 10000))

    fun entriesForDay(entries: List<FoodEntry>, day: LocalDate, zone: ZoneId): List<FoodEntry> =
        entries.filter { Instant.ofEpochMilli(it.loggedAtEpochMs).atZone(zone).toLocalDate() == day }

    fun totalCalories(entries: List<FoodEntry>): Double = entries.sumOf { it.energyKcal }
}
