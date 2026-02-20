package com.example.internetapi.resources

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.internetapi.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class UiStringsQualityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun keyMutationMessages_arePresentAndWithoutKnownTypos() {
        val ids = listOf(
            R.string.error_failed_remove_invoice,
            R.string.error_invalid_price_or_amount,
            R.string.error_failed_update_invoice_account,
            R.string.success_invoice_removed,
            R.string.success_income_added,
            R.string.success_money_transfer_completed
        )

        ids.forEach { id ->
            val value = context.getString(id)
            assertTrue(value.isNotBlank())
            assertFalse(containsTypoWord(value, "Faile"))
            assertFalse(containsTypoWord(value, "Invaliad"))
        }
    }

    @Test
    fun formattedErrorMessage_withName_isGenerated() {
        val value = context.getString(R.string.error_failed_add_new_media_with_name, "Water")

        assertTrue(value.contains("Water"))
    }

    private fun containsTypoWord(value: String, typo: String): Boolean {
        val pattern = Pattern.compile("\\b${Pattern.quote(typo)}\\b", Pattern.CASE_INSENSITIVE)
        return pattern.matcher(value).find()
    }
}
