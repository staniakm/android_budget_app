package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountOutcomeRegisterLaunchTest {

    @Test
    fun accountOutcomeRegisterActivity_launchesWithRequiredExtras() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, AccountOutcomeRegisterActivity::class.java).apply {
            putExtra("accountId", 1)
            putExtra("accountName", "Main")
        }

        ActivityScenario.launch<AccountOutcomeRegisterActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
