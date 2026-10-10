package com.maya.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** H2 opt-in barcode lookup. An identified product is never logged without confirmation. */
@Composable
fun BarcodeFoodPanel(onChoose: (String, Double, Double) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scanner = remember(context) { GmsBarcodeScanning.getClient(context) }
    var barcodeText by rememberSaveable { mutableStateOf("") }
    var portionText by rememberSaveable { mutableStateOf("100") }
    var product by remember { mutableStateOf<ProductSnapshot?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val portionG = FoodMath.parsePositiveDecimal(portionText, 10000.0)

    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Scanner un produit — H2", style = MaterialTheme.typography.titleMedium)
            Text(
                "Le scan utilise Google Play Services sur le téléphone. Seul le code-barres " +
                    "est envoyé à Open Food Facts après avoir appuyé sur Rechercher.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                enabled = !loading,
                onClick = {
                    scanner.startScan()
                        .addOnSuccessListener { scan ->
                            val raw = scan.rawValue?.trim() ?: ""
                            if (OpenFoodFactsClient.isValidBarcode(raw)) {
                                barcodeText = raw
                                product = null
                                message = "Code détecté. Appuie sur Rechercher pour consulter Open Food Facts."
                            } else {
                                message = "Ce code ne correspond pas à un code-barres alimentaire pris en charge."
                            }
                        }
                        .addOnCanceledListener { message = "Scan annulé." }
                        .addOnFailureListener {
                            message = "Scanner indisponible : vérifie Google Play Services ou saisis le code."
                        }
                }
            ) { Text("Scanner avec la caméra") }
            OutlinedTextField(
                value = barcodeText,
                onValueChange = { barcodeText = it.trim().take(14); product = null },
                label = { Text("Code EAN-8, UPC-A, EAN-13 ou GTIN-14") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Button(
                enabled = !loading && OpenFoodFactsClient.isValidBarcode(barcodeText),
                onClick = {
                    val code = barcodeText
                    loading = true
                    product = null
                    message = "Recherche du produit..."
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { OpenFoodFactsClient.fetchByCode(code) }
                        }
                        loading = false
                        result.onSuccess { found ->
                            product = found
                            message = if (found == null) "Produit non trouvé ; saisie manuelle possible."
                                else "Données collaboratives à contrôler sur l'étiquette."
                        }.onFailure {
                            message = "Recherche indisponible ; réessaie ou utilise la saisie manuelle."
                        }
                    }
                }
            ) { Text("Rechercher dans Open Food Facts") }
            if (message != null) Text(message!!, style = MaterialTheme.typography.bodySmall)
            val found = product
            if (found != null) {
                Text(found.name, style = MaterialTheme.typography.titleSmall)
                Text("Marque : " + (found.brand ?: "inconnue"))
                Text(
                    "Pour 100 g — énergie : " +
                        (found.energyKcal100g?.let { String.format(Locale.FRANCE, "%.1f kcal", it) } ?: "INCONNUE")
                )
                Text(
                    "Protéines : " + (found.proteinG100g?.toString() ?: "INCONNUES") +
                        " g ; glucides : " + (found.carbsG100g?.toString() ?: "INCONNUS") +
                        " g ; lipides : " + (found.fatG100g?.toString() ?: "INCONNUS") + " g"
                )
                Text("Source : Open Food Facts v3 · consultation " + found.capturedAt,
                    style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = portionText,
                    onValueChange = { portionText = it },
                    label = { Text("Portion réelle en grammes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Button(
                    enabled = portionG != null && found.energyKcal100g != null &&
                        !loading && found.code == barcodeText,
                    onClick = {
                        val kcal = found.energyKcal100g
                        val grams = portionG
                        if (kcal != null && grams != null) {
                            onChoose(found.name, kcal * grams / 100.0, grams)
                            message = "Champs préremplis. Vérifie la portion, puis enregistre le repas."
                        }
                    }
                ) { Text("Préremplir mon journal (à confirmer)") }
                if (found.energyKcal100g == null) {
                    Text("Calories manquantes : le système ne remplace pas une donnée absente par zéro.")
                }
            }
        }
    }
}
