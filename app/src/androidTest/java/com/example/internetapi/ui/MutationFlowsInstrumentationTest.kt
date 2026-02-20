package com.example.internetapi.ui

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.internetapi.R
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MutationFlowsInstrumentationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<AccountDetailsActivity>()

    @Test
    fun addIncomeMutationFlow_opensAddIncomeDialog() {
        val addIncomeLabel = composeRule.activity.getString(R.string.add_income)
        val addIncomeDialogTitle = composeRule.activity.getString(R.string.dialog_title_add_account_income)

        composeRule.onNodeWithText(addIncomeLabel).performClick()

        composeRule.onNodeWithText(addIncomeDialogTitle).assertIsDisplayed()
    }

    @Test
    fun transferMoneyMutationFlow_opensTransferDialog() {
        val transferMoneyLabel = composeRule.activity.getString(R.string.transfer_money)
        val transferMoneyDialogTitle = composeRule.activity.getString(R.string.dialog_title_transfer_money)

        composeRule.onNodeWithText(transferMoneyLabel).performClick()

        composeRule.onNodeWithText(transferMoneyDialogTitle).assertIsDisplayed()
    }

    @Test
    fun addIncomeMutationFlow_showsExpectedMutationInputs() {
        val addIncomeLabel = composeRule.activity.getString(R.string.add_income)
        val addIncomeDialogTitle = composeRule.activity.getString(R.string.dialog_title_add_account_income)
        val valueLabel = composeRule.activity.getString(R.string.label_value)
        val descriptionLabel = composeRule.activity.getString(R.string.label_description)

        composeRule.onNodeWithText(addIncomeLabel).performClick()
        composeRule.onNodeWithText(addIncomeDialogTitle).assertIsDisplayed()
        composeRule.onNodeWithText(valueLabel).assertIsDisplayed()
        composeRule.onNodeWithText(descriptionLabel).assertIsDisplayed()
    }

    @Test
    fun transferMutationFlow_showsExpectedMutationInputs() {
        val transferMoneyLabel = composeRule.activity.getString(R.string.transfer_money)
        val transferMoneyDialogTitle = composeRule.activity.getString(R.string.dialog_title_transfer_money)
        val valueLabel = composeRule.activity.getString(R.string.label_value)
        val targetAccountLabel = composeRule.activity.getString(R.string.label_target_account)

        composeRule.onNodeWithText(transferMoneyLabel).performClick()
        composeRule.onNodeWithText(transferMoneyDialogTitle).assertIsDisplayed()
        composeRule.onNodeWithText(valueLabel).assertIsDisplayed()
        composeRule.onNodeWithText(targetAccountLabel).assertIsDisplayed()
    }

    @Test
    fun accountOutcomeDetailsMutationScreen_launchesWithRequiredExtras() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, AccountOutcomeDetails::class.java).apply {
            putExtra("accountId", 1)
            putExtra("name", "Main")
            putExtra("outcome", "0.00")
        }

        ActivityScenario.launch<AccountOutcomeDetails>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
