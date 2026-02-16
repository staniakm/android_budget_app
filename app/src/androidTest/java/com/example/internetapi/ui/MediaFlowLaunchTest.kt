package com.example.internetapi.ui

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaFlowLaunchTest {

    @Test
    fun mediaActivity_launchesSuccessfully() {
        ActivityScenario.launch(MediaActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    fun mediaDetailsActivity_launchesWithExtras() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MediaDetailsActivity::class.java).apply {
            putExtra("mediaId", 1)
            putExtra("name", "Water")
        }

        ActivityScenario.launch<MediaDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertFalse(activity.isFinishing)
            }
        }
    }
}
