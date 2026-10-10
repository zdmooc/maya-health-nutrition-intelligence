package com.maya.health

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Run on an isolated CI emulator, with synthetic records only. */
@RunWith(AndroidJUnit4::class)
class EncryptedDiaryRepositoryTest {
    @Test fun encryptedDiarySurvivesReopenAndHasNoPlaintext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = EncryptedDiaryRepository(context)
        val previous = repo.load()
        val entry = FoodEntry.newManual("Synthetic-" + UUID.randomUUID().toString(), 123.0, 150.0)
        try {
            repo.save(previous + entry)
            val reloaded = EncryptedDiaryRepository(context).load()
            assertTrue(reloaded.any { it.id == entry.id })
            assertEquals(123.0, reloaded.first { it.id == entry.id }.energyKcal, 0.001)
            val file = File(context.noBackupFilesDir, "food_diary_v1.enc")
            assertTrue(file.exists())
            assertFalse(String(file.readBytes(), Charsets.ISO_8859_1).contains(entry.foodName))
        } finally {
            repo.save(previous)
        }
    }

    @Test fun passwordBackupRoundtripRejectsWrongPasswordAndTamper() {
        val payload = DiaryJsonCodec.encode(listOf(
            FoodEntry.newManual("FAKE-NUTRITION", 123.45, 88.0)
        ))
        val password = "Une-phrase-longue-de-test".toCharArray()
        val wrongPassword = "Mauvaise-phrase-secrete".toCharArray()
        try {
            val exported = PortableDiaryBackup.encrypt(payload, password)
            assertFalse(String(exported, Charsets.ISO_8859_1).contains("FAKE-NUTRITION"))
            val imported = DiaryJsonCodec.decode(PortableDiaryBackup.decrypt(exported, password))
            assertEquals("FAKE-NUTRITION", imported.single().foodName)
            assertEquals(123.45, imported.single().energyKcal, 0.001)
            assertReject { PortableDiaryBackup.decrypt(exported, wrongPassword) }
            val altered = exported.clone()
            altered[altered.lastIndex] = (altered.last().toInt() xor 0x7f).toByte()
            assertReject { PortableDiaryBackup.decrypt(altered, password) }
        } finally {
            password.fill('\u0000')
            wrongPassword.fill('\u0000')
        }
    }

    @Test fun fullPurgeDeletesCiphertextAndAllowsFreshKey() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = EncryptedDiaryRepository(context)
        val prior = repo.load()
        try {
            repo.save(listOf(FoodEntry.newManual("FAKE-DELETE", 5.0, null)))
            repo.deleteAll()
            assertFalse(File(context.noBackupFilesDir, "food_diary_v1.enc").exists())
            assertTrue(EncryptedDiaryRepository(context).load().isEmpty())
            val fresh = FoodEntry.newManual("FAKE-AFTER-ERASE", 25.0, null)
            repo.save(listOf(fresh))
            assertEquals(fresh.foodName, EncryptedDiaryRepository(context).load().single().foodName)
        } finally {
            // Test emulator only: restore initial fixtures to avoid test order dependence.
            repo.save(prior)
        }
    }

    @Test fun strictJsonRejectsInvalidUnitsAndSchema() {
        assertReject { DiaryJsonCodec.decode("""{"schemaVersion":9,"entries":[]}""".toByteArray()) }
        assertReject {
            DiaryJsonCodec.decode(
                """{"schemaVersion":1,"entries":[{"id":"x","foodName":"FAKE","energyKcal":-20,"portionGrams":null,"loggedAtEpochMs":1,"source":"USER_ENTERED"}]}""".toByteArray()
            )
        }
    }


    @Test fun offBarcodeParserDoesNotInventMissingNutritionValues() {
        val sample = """{"status":"success","product":{"code":"3017620422003","product_name":"Test fictif","nutriments":{"energy-kcal_100g":525.0,"proteins_100g":6.3}}}"""
        val found = OpenFoodFactsClient.parseResponse(sample, "3017620422003")
        assertEquals(525.0, found!!.energyKcal100g!!, 0.001)
        assertEquals(6.3, found.proteinG100g!!, 0.001)
        assertTrue(found.fatG100g == null)
        assertTrue(found.carbsG100g == null)
        assertTrue(OpenFoodFactsClient.isValidBarcode("3017620422003"))
        assertFalse(OpenFoodFactsClient.isValidBarcode("barcode-private"))
    }

    @Test fun offMissingOrWrongProductIsRejected() {
        assertTrue(OpenFoodFactsClient.parseResponse("""{"status":"failure"}""", "3017620422003") == null)
        assertReject {
            OpenFoodFactsClient.parseResponse(
                """{"status":"success","product":{"code":"0000000000000","product_name":"Other"}}""",
                "3017620422003"
            )
        }
    }

    private fun assertReject(block: () -> Unit) {
        var thrown = false
        try { block() } catch (_: Exception) { thrown = true }
        assertTrue("Expected invalid payload to be rejected", thrown)
    }
}
