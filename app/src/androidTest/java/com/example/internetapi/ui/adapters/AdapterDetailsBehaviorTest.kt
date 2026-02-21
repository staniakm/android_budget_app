package com.example.internetapi.ui.adapters

import android.content.Intent
import android.widget.FrameLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.internetapi.api.CategoryApiHelper
import com.example.internetapi.models.Category
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.repository.CategoryRepository
import com.example.internetapi.ui.CategoryDetailsActivity
import com.example.internetapi.ui.viewModel.CategoryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
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

    @Test
    fun categoryDetailsAdapter_submitList_updatesCount() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, CategoryDetailsActivity::class.java).apply {
            putExtra("categoryId", 1)
            putExtra("name", "Food")
        }

        ActivityScenario.launch<CategoryDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = CategoryViewModel(CategoryRepository(FakeCategoryApiHelper()))
                val adapter = CategoryDetailsAdapter(viewModel, activity)
                adapter.submitList(
                    listOf(
                        CategoryDetails(1, "Milk", BigDecimal("8.50")),
                        CategoryDetails(2, "Bread", BigDecimal("4.20"))
                    )
                )

                await { adapter.itemCount == 2 }
                assertEquals(2, adapter.itemCount)
            }
        }
    }

    @Test
    fun categoryDetailsAdapter_onBind_setsBasicFields() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, CategoryDetailsActivity::class.java).apply {
            putExtra("categoryId", 1)
            putExtra("name", "Food")
        }

        ActivityScenario.launch<CategoryDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val viewModel = CategoryViewModel(CategoryRepository(FakeCategoryApiHelper()))
                val adapter = CategoryDetailsAdapter(viewModel, activity)
                adapter.submitList(listOf(CategoryDetails(7, "Coffee", BigDecimal("15.90"))))
                await { adapter.itemCount == 1 }

                val parent = FrameLayout(activity)
                val holder = adapter.onCreateViewHolder(parent, 0)
                adapter.onBindViewHolder(holder, 0)

                assertEquals("Coffee", holder.binding.assortmentName.text.toString())
                assertEquals("15.90", holder.binding.month.text.toString())
            }
        }
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

private class FakeCategoryApiHelper : CategoryApiHelper {
    override suspend fun getCategories(): Response<List<Category>> = Response.success(emptyList())

    override suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>> = Response.success(emptyList())
}
