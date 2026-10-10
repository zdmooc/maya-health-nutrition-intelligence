package com.maya.health

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

/** Single strict schema for encrypted local files and portable encrypted archives. */
object DiaryJsonCodec {
    private const val MAX_ENTRIES = 10_000
    private const val MAX_JSON_BYTES = 5_000_000

    fun encode(entries: List<FoodEntry>): ByteArray {
        require(entries.size <= MAX_ENTRIES) { "Diary limit exceeded" }
        val array = JSONArray()
        val seen = mutableSetOf<String>()
        entries.forEach { row ->
            require(row.id.isNotBlank() && seen.add(row.id) && row.source == "USER_ENTERED")
            require(FoodMath.isValid(row.foodName, row.energyKcal, row.portionGrams,
                row.portionGrams?.toString() ?: ""))
            require(row.loggedAtEpochMs > 0L)
            array.put(JSONObject().apply {
                put("id", row.id)
                put("foodName", row.foodName)
                put("energyKcal", row.energyKcal)
                put("portionGrams", row.portionGrams ?: JSONObject.NULL)
                put("loggedAtEpochMs", row.loggedAtEpochMs)
                put("source", row.source)
            })
        }
        val bytes = JSONObject().put("schemaVersion", 1).put("entries", array)
            .toString().toByteArray(StandardCharsets.UTF_8)
        require(bytes.size <= MAX_JSON_BYTES) { "Diary file too large" }
        return bytes
    }

    fun decode(payload: ByteArray): List<FoodEntry> {
        if (payload.size > MAX_JSON_BYTES) throw IOException("Diary file too large")
        try {
            val obj = JSONObject(String(payload, StandardCharsets.UTF_8))
            if (obj.getInt("schemaVersion") != 1) throw IOException("Unknown diary schema")
            val array = obj.getJSONArray("entries")
            if (array.length() > MAX_ENTRIES) throw IOException("Diary limit exceeded")
            val entries = ArrayList<FoodEntry>(array.length())
            val ids = mutableSetOf<String>()
            for (i in 0 until array.length()) {
                val row = array.getJSONObject(i)
                val grams = if (row.isNull("portionGrams")) null else row.getDouble("portionGrams")
                val entry = FoodEntry(
                    id = row.getString("id"),
                    foodName = row.getString("foodName"),
                    energyKcal = row.getDouble("energyKcal"),
                    portionGrams = grams,
                    loggedAtEpochMs = row.getLong("loggedAtEpochMs"),
                    source = row.getString("source")
                )
                if (!ids.add(entry.id) || entry.id.isBlank() || entry.loggedAtEpochMs <= 0L ||
                    entry.source != "USER_ENTERED" ||
                    !FoodMath.isValid(entry.foodName, entry.energyKcal,
                        entry.portionGrams, entry.portionGrams?.toString() ?: "")) {
                    throw IOException("Invalid diary record")
                }
                entries.add(entry)
            }
            return entries
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw IOException("Invalid diary data", e)
        }
    }
}
