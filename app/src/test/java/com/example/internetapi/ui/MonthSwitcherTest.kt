package com.example.internetapi.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class MonthSwitcherTest {

    @Test
    fun monthSwitcherLabels_usesChartsStyleDateOffsets() {
        val base = LocalDate.of(2026, 2, 20)

        val labels = monthSwitcherLabels(baseDate = base, monthOffset = 0)

        assertEquals("2026-01", labels.previous)
        assertEquals("2026-02", labels.current)
        assertEquals("2026-03", labels.next)
    }

    @Test
    fun monthSwitcherLabels_appliesMonthOffset() {
        val base = LocalDate.of(2026, 2, 20)

        val labels = monthSwitcherLabels(baseDate = base, monthOffset = -2)

        assertEquals("2025-11", labels.previous)
        assertEquals("2025-12", labels.current)
        assertEquals("2026-01", labels.next)
    }

    @Test
    fun monthSwitcherLabels_handlesYearBoundaryForward() {
        val base = LocalDate.of(2026, 12, 31)

        val labels = monthSwitcherLabels(baseDate = base, monthOffset = 1)

        assertEquals("2026-12", labels.previous)
        assertEquals("2027-01", labels.current)
        assertEquals("2027-02", labels.next)
    }

    @Test
    fun monthSwitcherLabels_normalizesToFirstDayBeforeOffset() {
        val base = LocalDate.of(2026, 3, 31)

        val labels = monthSwitcherLabels(baseDate = base, monthOffset = -1)

        assertEquals("2026-01", labels.previous)
        assertEquals("2026-02", labels.current)
        assertEquals("2026-03", labels.next)
    }
}
