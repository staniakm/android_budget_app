package com.example.internetapi.ui.adapters

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.MediaUsage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class AdapterDetailsBehaviorTest {

    @Test
    fun invoiceDetailsAdapter_submitList_updatesItemCount() {
        val adapter = InvoiceDetailsAdapter()

        adapter.submitList(
            listOf(
                InvoiceDetails(1, "Milk", BigDecimal("2.000"), BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal("20.00")),
                InvoiceDetails(2, "Bread", BigDecimal("1.000"), BigDecimal("4.00"), BigDecimal.ZERO, BigDecimal("4.00"))
            )
        )

        await { adapter.itemCount == 2 }
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun mediaDetailsAdapter_removeAt_returnsRemovedItemAndShrinksList() {
        val adapter = MediaDetailsAdapter()
        adapter.submitList(
            listOf(
                MediaUsage(10, 2026, 2, BigDecimal("320.0")),
                MediaUsage(9, 2026, 1, BigDecimal("300.0"))
            )
        )
        await { adapter.itemCount == 2 }

        val removed = adapter.removeAt(0)
        await { adapter.itemCount == 1 }

        assertEquals(10, removed?.id)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun mediaDetailsAdapter_removeAt_keepsRemainingOrder() {
        val adapter = MediaDetailsAdapter()
        adapter.submitList(
            listOf(
                MediaUsage(10, 2026, 2, BigDecimal("320.0")),
                MediaUsage(9, 2026, 1, BigDecimal("300.0")),
                MediaUsage(8, 2025, 12, BigDecimal("280.0"))
            )
        )
        await { adapter.itemCount == 3 }

        adapter.removeAt(1)
        await { adapter.itemCount == 2 }
        val nextRemoved = adapter.removeAt(1)

        assertEquals(8, nextRemoved?.id)
    }

    private fun await(timeoutMs: Long = 2_000, condition: () -> Boolean) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            instrumentation.waitForIdleSync()
            if (condition()) return
            Thread.sleep(25)
        }
        assertTrue("Condition not met within timeout", condition())
    }
}
