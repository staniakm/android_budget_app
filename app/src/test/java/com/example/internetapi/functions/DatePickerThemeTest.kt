package com.example.internetapi.functions

import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DatePickerThemeTest {

    @Test
    fun resolveDatePickerThemeResId_returnsLightThemeInNightMode() {
        val result = resolveDatePickerThemeResId(Configuration.UI_MODE_NIGHT_YES)

        assertEquals(android.R.style.Theme_DeviceDefault_Light, result)
    }

    @Test
    fun resolveDatePickerThemeResId_returnsNullInDayMode() {
        val result = resolveDatePickerThemeResId(Configuration.UI_MODE_NIGHT_NO)

        assertNull(result)
    }
}
