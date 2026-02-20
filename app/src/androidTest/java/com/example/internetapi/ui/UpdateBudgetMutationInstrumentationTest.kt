package com.example.internetapi.ui

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.internetapi.R
import com.example.internetapi.models.MonthBudget
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class UpdateBudgetMutationInstrumentationTest {

    private val launchIntent = Intent(
        ApplicationProvider.getApplicationContext(),
        UpdateBudgetActivity::class.java
    ).apply {
        putExtra(
            "budget",
            MonthBudget(
                budgetId = 1,
                category = "Food",
                spent = BigDecimal("120.00"),
                planned = BigDecimal("300.00"),
                percentage = 40
            )
        )
    }

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun editPlannedMutationFlow_opensUpdateDialog() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val editLabel = context.getString(R.string.edit)

        ActivityScenario.launch<UpdateBudgetActivity>(launchIntent).use {
            composeRule.waitForIdle()
            composeRule.onNodeWithText(editLabel).performClick()

            composeRule.onNodeWithText("Update planned value for budget").assertIsDisplayed()
            composeRule.onNodeWithText("Planned").assertIsDisplayed()
        }
    }
}
