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

    @Test
    fun resolveDatePickerThemeResId_returnsLightThemeWhenNightBitIsPresentWithOtherFlags() {
        val uiMode = Configuration.UI_MODE_TYPE_NORMAL or Configuration.UI_MODE_NIGHT_YES

        val result = resolveDatePickerThemeResId(uiMode)

        assertEquals(android.R.style.Theme_DeviceDefault_Light, result)
    }

    @Test
    fun resolveDatePickerThemeResId_returnsNullWhenNightModeIsUndefined() {
        val uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED

        val result = resolveDatePickerThemeResId(uiMode)

        assertNull(result)
    }
}
