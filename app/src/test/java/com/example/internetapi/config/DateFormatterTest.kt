package com.example.internetapi.config

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DateFormatterTest {

    @Test
    fun yyyymm_formatsYearMonthAsExpected() {
        val formatted = LocalDate.of(2026, 2, 20).format(DateFormatter.yyyymm)

        assertEquals("2026-02", formatted)
    }
}
