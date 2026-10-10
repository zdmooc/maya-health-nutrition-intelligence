package com.maya.health

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.Instant

/**
 * H2: only the product barcode is sent to Open Food Facts upon explicit user action.
 * Any nutrient not present in the response stays null / UNKNOWN (never zero).
 */
data class ProductSnapshot(
    val code: String,
    val name: String,
    val brand: String?,
    val energyKcal100g: Double?,
    val proteinG100g: Double?,
    val fatG100g: Double?,
    val carbsG100g: Double?,
    val capturedAt: String,
    val source: String = "OPEN_FOOD_FACTS_V3"
)

object OpenFoodFactsClient {
    private const val MAX_RESPONSE = 1_000_000
    private const val USER_AGENT = "MayaHealthNutrition/0.3 (https://github.com/zdmooc/maya-health-nutrition-intelligence)"

    fun isValidBarcode(code: String): Boolean =
        code.matches(Regex("(\\d{8}|\\d{12}|\\d{13}|\\d{14})"))

    fun fetchByCode(code: String): ProductSnapshot? {
        require(isValidBarcode(code)) { "EAN/UPC invalid" }
        val url = URL("https://world.openfoodfacts.org/api/v3/product/" + code +
            "?fields=code,product_name,product_name_fr,brands,nutriments")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 8_000
        connection.readTimeout = 8_000
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", "application/json")
        try {
            val status = connection.responseCode
            if (status == 404) return null
            if (status != 200) throw IOException("Open Food Facts HTTP " + status)
            val jsonBytes = connection.inputStream.use { input ->
                val out = ByteArrayOutputStream()
                val block = ByteArray(8192)
                while (true) {
                    val size = input.read(block)
                    if (size < 0) break
                    if (out.size() + size > MAX_RESPONSE) throw IOException("Response too large")
                    out.write(block, 0, size)
                }
                out.toByteArray()
            }
            return parseResponse(String(jsonBytes, StandardCharsets.UTF_8), code)
        } finally {
            connection.disconnect()
        }
    }

    fun parseResponse(json: String, code: String): ProductSnapshot? {
        if (!isValidBarcode(code)) throw IOException("Invalid barcode")
        val root = JSONObject(json)
        if (root.optString("status") == "failure" || !root.has("product") ||
            root.isNull("product")) return null
        val product = root.getJSONObject("product")
        val resolvedCode = product.optString("code", code)
        if (resolvedCode != code) throw IOException("Barcode mismatch")
        val name = product.optString("product_name_fr")
            .ifBlank { product.optString("product_name") }
            .ifBlank { "Produit sans nom" }.take(120)
        val nuts = product.optJSONObject("nutriments")
        fun value(key: String): Double? {
            if (nuts == null || !nuts.has(key) || nuts.isNull(key)) return null
            val v = nuts.optDouble(key, Double.NaN)
            return v.takeIf { it.isFinite() && it >= 0.0 && it <= 100_000.0 }
        }
        return ProductSnapshot(
            code = code,
            name = name,
            brand = product.optString("brands").ifBlank { null },
            energyKcal100g = value("energy-kcal_100g"),
            proteinG100g = value("proteins_100g"),
            fatG100g = value("fat_100g"),
            carbsG100g = value("carbohydrates_100g"),
            capturedAt = Instant.now().toString()
        )
    }
}
