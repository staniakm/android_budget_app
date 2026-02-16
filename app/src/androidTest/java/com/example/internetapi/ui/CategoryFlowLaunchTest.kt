package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryFlowLaunchTest {

    @Test
    fun categoryActivity_launchesSuccessfully() {
        ActivityScenario.launch(CategoryActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    fun categoryDetailsActivity_launchesWithExtras() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, CategoryDetailsActivity::class.java).apply {
            putExtra("categoryId", 1)
            putExtra("name", "Food")
        }

        ActivityScenario.launch<CategoryDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
