package com.maya.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Uniquement le démonstrateur H0 : journal manuel et temporaire. Aucune donnée de santé. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FoodDiaryScreen()
            }
        }
    }
}

private data class FoodEntry(val name: String, val estimatedCalories: Double)

@Composable
private fun FoodDiaryScreen() {
    var name by rememberSaveable { mutableStateOf("") }
    var caloriesText by rememberSaveable { mutableStateOf("") }
    val entries = remember { mutableStateListOf<FoodEntry>() }
    val calories = caloriesText.replace(',', '.').toDoubleOrNull()
    val valid = name.isNotBlank() && calories != null && calories.isFinite() && calories >= 0.0 && calories <= 10000.0
    val total = entries.sumOf { it.estimatedCalories }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Maya Santé", style = MaterialTheme.typography.headlineMedium)
        Text("Prototype Android H0 — journal alimentaire manuel", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Démonstration locale uniquement : rien n'est sauvegardé. N'ajoute pas de données médicales personnelles.",
                modifier = Modifier.padding(16.dp)
            )
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Aliment (fictif pour les tests)") },
            singleLine = true
        )
        OutlinedTextField(
            value = caloriesText,
            onValueChange = { caloriesText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Énergie indiquée par toi (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        Button(
            onClick = {
                if (valid) {
                    entries.add(FoodEntry(name.trim(), calories!!))
                    name = ""
                    caloriesText = ""
                }
            },
            enabled = valid
        ) {
            Text("Ajouter au journal temporaire")
        }
        HorizontalDivider()
        Text("Total saisi : ${total.toInt()} kcal", style = MaterialTheme.typography.titleLarge)
        Text("Minéraux et vitamines : inconnus (non mesurés)", style = MaterialTheme.typography.bodyMedium)
        Text("Les calories saisies ne sont pas une estimation calculée par l'application.")
        entries.forEach { entry ->
            Text("• ${entry.name} — ${entry.estimatedCalories.toInt()} kcal")
        }
        if (entries.isNotEmpty()) {
            Button(onClick = { entries.clear() }) {
                Text("Vider le journal temporaire")
            }
        }
    }
}
