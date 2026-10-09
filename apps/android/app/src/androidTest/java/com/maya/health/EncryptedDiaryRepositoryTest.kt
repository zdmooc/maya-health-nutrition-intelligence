package com.maya.health

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * AndroidKeyStore is only available on a real device/emulator, not a local JVM.
 * This suite MUST run before H1 is declared DEVICE_RUNTIME_PROVEN.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedDiaryRepositoryTest {
    @Test fun encryptPersistAndReadBack() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo = EncryptedDiaryRepository(context)
        val old = repo.load()
        val fixture = FoodEntry.newManual("Fictif ${UUID.randomUUID()}", 123.0, 150.0)
        try {
            repo.save(old + fixture)
            val reloaded = EncryptedDiaryRepository(context).load()
            assertTrue(reloaded.any { it.id == fixture.id })
            assertEquals(123.0, reloaded.first { it.id == fixture.id }.energyKcal, 0.001)
            // Raw encrypted bytes must not contain the food name as plaintext.
            val file = java.io.File(context.noBackupFilesDir, "food_diary_v1.enc")
            assertTrue(!String(file.readBytes(), Charsets.UTF_8).contains(fixture.foodName))
        } finally {
            repo.save(old)
        }
    }
}
