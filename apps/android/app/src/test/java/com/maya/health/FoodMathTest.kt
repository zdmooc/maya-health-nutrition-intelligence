package com.maya.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class FoodMathTest {
    @Test fun frenchCommaDecimalsAndInvalidInputs() {
        assertEquals(135.5, FoodMath.parseCalories("135,5")!!, 0.0001)
        assertEquals(0.0, FoodMath.parseCalories("0")!!, 0.0001)
        assertNull(FoodMath.parseCalories("-25"))
        assertNull(FoodMath.parseCalories("NaN"))
        assertNull(FoodMath.parseCalories("10001"))
        assertNull(FoodMath.parseCalories("1e4"))
        assertEquals(175.4, FoodMath.parsePositiveDecimal("175,4", 10000.0)!!, 0.0001)
        assertNull(FoodMath.parsePositiveDecimal("0", 10000.0))
    }
    @Test fun inputValidation() {
        assertTrue(FoodMath.isValid("Pois chiches", 120.0, 150.0, "150"))
        assertTrue(FoodMath.isValid("Fruits rouges", 0.0, null, ""))
        assertFalse(FoodMath.isValid("", 10.0, null, ""))
        assertFalse(FoodMath.isValid("Skyr", null, null, ""))
        assertFalse(FoodMath.isValid("Skyr", 10.0, null, "abc"))
        assertFalse(FoodMath.isValid("Skyr", 10.0, -1.0, "-1"))
    }
    @Test fun dailyTotalsFollowLocalTimezoneNotUtc() {
        val paris = ZoneId.of("Europe/Paris")
        val stamp = ZonedDateTime.of(2026, 10, 9, 0, 10, 0, 0, paris).toInstant().toEpochMilli()
        val rows = listOf(FoodEntry("a", "Fictif", 150.0, null, stamp))
        assertEquals(150.0, FoodMath.totalCalories(FoodMath.entriesForDay(rows, LocalDate.of(2026,10,9), paris)), 0.0001)
        assertEquals(0.0, FoodMath.totalCalories(FoodMath.entriesForDay(rows, LocalDate.of(2026,10,8), paris)), 0.0001)
    }
}
