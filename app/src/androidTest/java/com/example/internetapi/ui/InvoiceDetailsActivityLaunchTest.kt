package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InvoiceDetailsActivityLaunchTest {

    @Test
    fun invoiceDetailsActivity_launchesWithInvoiceId() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, InvoiceDetailsActivity::class.java).apply {
            putExtra("invoiceId", 1L)
        }

        ActivityScenario.launch<InvoiceDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
