package com.maya.health

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = EncryptedDiaryRepository(applicationContext)
        setContent {
            MaterialTheme {
                FoodDiaryScreen(repository)
            }
        }
    }
}

@Composable
private fun FoodDiaryScreen(repository: EncryptedDiaryRepository) {
    // No silent reset on keystore loss or authentication failure.
    val firstRead = remember { runCatching { repository.load() } }
    var entries by remember { mutableStateOf(firstRead.getOrDefault(emptyList())) }
    var storageError by remember {
        mutableStateOf(
            if (firstRead.isFailure) "Le journal chiffré est illisible. Aucune écriture n'est autorisée." else null
        )
    }
    var name by rememberSaveable { mutableStateOf("") }
    var kcalText by rememberSaveable { mutableStateOf("") }
    var gramsText by rememberSaveable { mutableStateOf("") }
    var backupPassword by remember { mutableStateOf("") }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var approveOverwrite by remember { mutableStateOf(false) }
    var approveErase by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // SAF creates a document only after the person chooses the destination.
    // Backup is encrypted before a single byte is written to that destination.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            val secret = backupPassword.toCharArray()
            try {
                val archive = PortableDiaryBackup.encrypt(DiaryJsonCodec.encode(entries), secret)
                val output = context.contentResolver.openOutputStream(uri, "w")
                    ?: throw IOException("Export destination unavailable")
                output.use { it.write(archive) }
                backupStatus = "Export chiffré créé. Conserve ton mot de passe : il ne peut pas être récupéré."
            } catch (_: Exception) {
                backupStatus = "Export impossible. Supprime le fichier partiel éventuel dans le stockage choisi."
            } finally {
                secret.fill('\u0000')
                backupPassword = ""
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val secret = backupPassword.toCharArray()
            try {
                if (!approveOverwrite || storageError != null) {
                    throw IOException("Replacement is not authorized")
                }
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IOException("Import source unavailable")
                val encrypted = input.use { PortableDiaryBackup.readLimited(it) }
                val restored = DiaryJsonCodec.decode(PortableDiaryBackup.decrypt(encrypted, secret))
                // Validate and decrypt everything before changing the current diary.
                repository.save(restored)
                entries = restored
                backupStatus = "Journal importé et enregistré localement."
                storageError = null
                approveOverwrite = false
            } catch (_: Exception) {
                backupStatus = "Import refusé : mot de passe, format ou stockage invalide. Journal inchangé."
            } finally {
                secret.fill('\u0000')
                backupPassword = ""
            }
        }
    }

    val kcal = FoodMath.parseCalories(kcalText)
    val grams = if (gramsText.isBlank()) null else FoodMath.parsePositiveDecimal(gramsText, 10000.0)
    val valid = FoodMath.isValid(name, kcal, grams, gramsText)
    val zone = remember { ZoneId.systemDefault() }
    val today = LocalDate.now(zone)
    val currentDay = FoodMath.entriesForDay(entries, today, zone)
    val caloriesToday = FoodMath.totalCalories(currentDay)
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.FRANCE) }

    fun persist(next: List<FoodEntry>) {
        runCatching { repository.save(next) }
            .onSuccess { entries = next; storageError = null }
            .onFailure {
                storageError = "Sauvegarde impossible. Modifications non enregistrées. Réessaie sans effacer les données."
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Maya Santé", style = MaterialTheme.typography.headlineMedium)
        Text("Mon journal alimentaire", style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Aujourd'hui — ${today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                Text("${String.format(Locale.FRANCE, "%.1f", caloriesToday)} kcal", style = MaterialTheme.typography.headlineSmall)
                Text("${currentDay.size} aliment(s) saisi(s) · uniquement des calories renseignées manuellement")
            }
        }
        Text(
            "H1 • Journal conservé et chiffré sur ce téléphone. Sans réseau. " +
                "Aucune estimation automatique, aucun diagnostic.",
            style = MaterialTheme.typography.bodySmall
        )
        if (storageError != null) {
            Text(storageError!!, color = MaterialTheme.colorScheme.error)
        }
        Text("Ajouter un aliment", style = MaterialTheme.typography.titleMedium)
        BarcodeFoodPanel(onChoose = { label, kcal, grams ->
            name = label.take(120)
            kcalText = String.format(Locale.US, "%.2f", kcal)
            gramsText = String.format(Locale.US, "%.2f", grams)
        })
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(120) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Aliment") },
            singleLine = true
        )
        OutlinedTextField(
            value = kcalText,
            onValueChange = { kcalText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Calories pour cette portion (kcal)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = gramsText,
            onValueChange = { gramsText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Poids de la portion en g (facultatif)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        Button(
            onClick = {
                if (valid && kcal != null && storageError == null) {
                    val next = entries + FoodEntry.newManual(name, kcal, grams)
                    persist(next)
                    if (storageError == null) {
                        name = ""
                        kcalText = ""
                        gramsText = ""
                    }
                }
            },
            enabled = valid && storageError == null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Enregistrer dans mon journal") }
        HorizontalDivider()
        Text("Historique récent", style = MaterialTheme.typography.titleMedium)
        if (entries.isEmpty()) Text("Aucun aliment enregistré.")
        entries.sortedByDescending { it.loggedAtEpochMs }.take(100).forEach { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.foodName, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${item.energyKcal} kcal · ${item.portionGrams?.let { "${it} g" } ?: "portion non pesée"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            Instant.ofEpochMilli(item.loggedAtEpochMs).atZone(zone).format(dateFmt),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        enabled = storageError == null,
                        onClick = { persist(entries.filterNot { it.id == item.id }) }
                    ) { Text("Supprimer") }
                }
            }
        }
        HorizontalDivider()
        Text("Sauvegarde et confidentialité", style = MaterialTheme.typography.titleMedium)
        Text(
            "Les exports sont chiffrés avec une phrase secrète (12 caractères minimum). " +
                "Sans cette phrase, la restauration est impossible. Ne la communique jamais.",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = backupPassword,
            onValueChange = { backupPassword = it.take(256) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phrase secrète pour sauvegarder/restaurer") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )
        Button(
            enabled = backupPassword.length >= 12 && storageError == null,
            onClick = { exportLauncher.launch("maya-health-backup.mhbackup") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Exporter un fichier chiffré") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Checkbox(
                checked = approveOverwrite,
                onCheckedChange = { approveOverwrite = it }
            )
            Text(
                "Je confirme : importer remplacera tout le journal existant.",
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            enabled = backupPassword.length >= 12 && approveOverwrite && storageError == null,
            onClick = { importLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Importer une sauvegarde chiffrée") }
        if (backupStatus != null) {
            Text(backupStatus!!, style = MaterialTheme.typography.bodySmall)
        }
        HorizontalDivider()
        Text("Suppression complète", style = MaterialTheme.typography.titleMedium)
        Text(
            "Efface définitivement le journal local et sa clé de chiffrement. " +
                "Les exports créés ailleurs ne sont pas supprimés.",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Checkbox(
                checked = approveErase,
                onCheckedChange = { approveErase = it }
            )
            Text("Je confirme la suppression irréversible du journal local.")
        }
        Button(
            enabled = approveErase,
            onClick = {
                runCatching { repository.deleteAll() }
                    .onSuccess {
                        entries = emptyList()
                        storageError = null
                        backupPassword = ""
                        backupStatus = "Journal local supprimé."
                        approveErase = false
                        approveOverwrite = false
                    }
                    .onFailure {
                        backupStatus = "Suppression incomplète : vérifier le stockage avant réutilisation."
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Effacer définitivement le journal") }
        Text(
            "Valeurs saisies par l'utilisateur, non vérifiées. Les nutriments non renseignés " +
                "(fer, calcium, magnésium, vitamines…) restent INCONNUS : jamais zéro.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
