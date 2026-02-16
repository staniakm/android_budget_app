package com.example.internetapi.resources

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.internetapi.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiStringsQualityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun keyMutationMessages_arePresentAndWithoutKnownTypos() {
        val ids = listOf(
            R.string.error_failed_remove_invoice,
            R.string.error_invalid_price_or_amount,
            R.string.error_failed_update_invoice_account,
            R.string.success_income_added,
            R.string.success_money_transfer_completed
        )

        ids.forEach { id ->
            val value = context.getString(id)
            assertTrue(value.isNotBlank())
            assertFalse(value.contains("Faile", ignoreCase = true))
            assertFalse(value.contains("Invaliad", ignoreCase = true))
        }
    }

    @Test
    fun formattedErrorMessage_withName_isGenerated() {
        val value = context.getString(R.string.error_failed_add_new_media_with_name, "Water")

        assertTrue(value.contains("Water"))
    }
}
