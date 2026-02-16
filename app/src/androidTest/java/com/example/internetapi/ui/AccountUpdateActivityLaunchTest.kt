package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.internetapi.models.Account
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class AccountUpdateActivityLaunchTest {

    @Test
    fun accountUpdateActivity_launchesWithSerializableAccountExtra() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, AccountUpdateActivity::class.java).apply {
            putExtra(
                "account",
                Account(
                    id = 1,
                    name = "Main",
                    moneyAmount = BigDecimal("100.00"),
                    expense = BigDecimal("20.00"),
                    income = BigDecimal("50.00")
                )
            )
        }

        ActivityScenario.launch<AccountUpdateActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
