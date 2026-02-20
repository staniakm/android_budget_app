package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.internetapi.models.MonthBudget
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class UpdateBudgetActivityLaunchTest {

    @Test
    fun updateBudgetActivity_launchesWithMonthBudgetExtra() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, UpdateBudgetActivity::class.java).apply {
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

        ActivityScenario.launch<UpdateBudgetActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    fun updateBudgetActivity_withoutBudgetExtra_finishesImmediately() {
        ActivityScenario.launch(UpdateBudgetActivity::class.java).use { scenario ->
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertTrue(scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED)
        }
    }
}
