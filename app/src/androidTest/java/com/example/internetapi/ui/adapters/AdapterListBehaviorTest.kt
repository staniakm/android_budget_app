package com.example.internetapi.ui.adapters

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.internetapi.models.Account
import com.example.internetapi.models.Category
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.models.UpdateBudgetResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class AdapterListBehaviorTest {

    private val listener = object : OnItemClickedListener {
        override fun onClick(position: Int, element: String) = Unit
    }

    @Test
    fun mediaAdapter_addNewMediaType_appendsItem() {
        val adapter = MediaAdapter(listener)
        adapter.submitList(listOf(MediaType(1, "Water")))
        await { adapter.itemCount == 1 }

        adapter.addNewMediaType(MediaType(2, "Gas"))
        await { adapter.itemCount == 2 }

        assertEquals("Gas", adapter.getItem(1).name)
    }

    @Test
    fun categoryAdapter_addNewCategory_appendsItem() {
        val adapter = CategoryAdapter(listener)
        adapter.submitList(
            listOf(
                Category(1, "Food", BigDecimal("120.00"), BigDecimal("450.00"))
            )
        )
        await { adapter.itemCount == 1 }

        adapter.addNewCategory(Category(2, "Transport", BigDecimal("60.00"), BigDecimal("200.00")))
        await { adapter.itemCount == 2 }

        assertEquals("Transport", adapter.getItem(1).name)
    }

    @Test
    fun accountAdapter_updateListItem_updatesAndSortsByName() {
        val adapter = AccountAdapter(listener)
        adapter.submitList(
            listOf(
                Account(2, "Zoo", BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO),
                Account(1, "Main", BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO)
            )
        )
        await { adapter.itemCount == 2 }

        adapter.updateListItem(UpdateAccountResponse(2, "Alpha", BigDecimal("999.00")))
        await { adapter.getItem(0).name == "Alpha" }

        assertEquals(2, adapter.getItem(0).id)
        assertEquals(0, adapter.getItem(0).moneyAmount.compareTo(BigDecimal("999.00")))
        assertEquals("Main", adapter.getItem(1).name)
    }

    @Test
    fun monthBudgetAdapter_updateBudget_updatesOnlyMatchingItem() {
        val adapter = MonthBudgetAdapter(listener)
        adapter.submitList(
            listOf(
                MonthBudget(1, "Food", BigDecimal("100.00"), BigDecimal("200.00"), 50),
                MonthBudget(2, "Home", BigDecimal("80.00"), BigDecimal("160.00"), 50)
            )
        )
        await { adapter.itemCount == 2 }

        adapter.updateBudget(
            UpdateBudgetResponse(
                budgetId = 2,
                category = "Home",
                spent = BigDecimal("80.00"),
                planned = BigDecimal("300.00"),
                monthPlanned = BigDecimal("500.00"),
                percentage = 27
            )
        )
        await { adapter.getItem(1).planned.compareTo(BigDecimal("300.00")) == 0 }

        assertEquals(0, adapter.getItem(0).planned.compareTo(BigDecimal("200.00")))
        assertEquals(0, adapter.getItem(1).planned.compareTo(BigDecimal("300.00")))
        assertEquals(27, adapter.getItem(1).percentage)
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
