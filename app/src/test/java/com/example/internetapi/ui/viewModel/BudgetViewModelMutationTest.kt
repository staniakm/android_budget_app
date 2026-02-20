package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.BudgetApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Budget
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.MonthBudget
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateBudgetRequest
import com.example.internetapi.models.UpdateBudgetResponse
import com.example.internetapi.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelMutationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("BudgetMutationTestMain")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThread)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThread.close()
    }

    @Test
    fun updateBudget_emitsSuccessWithUpdatedBudgetResponse() {
        val response = UpdateBudgetResponse(
            budgetId = 10,
            category = "Food",
            spent = BigDecimal("120.00"),
            planned = BigDecimal("300.00"),
            monthPlanned = BigDecimal("1200.00"),
            percentage = 40
        )
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(updateResponse = Response.success(response))
        )

        val values = viewModel.updateBudget(
            UpdateBudgetRequest(budgetId = 10, planned = BigDecimal("300.00"))
        ).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(response, values.last().data)
    }

    @Test
    fun recalculateBudgets_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(recalculateResponse = errorResponse())
        )

        val values = viewModel.recalculateBudgets().awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun recalculateBudgets_emitsSuccessWithBudgetData() {
        val expected = Budget(
            totalSpend = BigDecimal("100.00"),
            totalPlanned = BigDecimal("300.00"),
            totalEarned = BigDecimal("500.00"),
            budgets = listOf(
                MonthBudget(
                    budgetId = 10,
                    category = "Food",
                    spent = BigDecimal("100.00"),
                    planned = BigDecimal("300.00"),
                    percentage = 33
                )
            )
        )
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(recalculateResponse = Response.success(expected))
        )

        val values = viewModel.recalculateBudgets().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun updateBudget_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(updateResponse = errorResponse())
        )

        val values = viewModel.updateBudget(
            UpdateBudgetRequest(budgetId = 10, planned = BigDecimal("300.00"))
        ).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getBudgetItems_emitsSuccessWithReturnedItems() {
        val expected = listOf(
            InvoiceDetails(
                invoiceItemId = 1,
                productName = "Milk",
                quantity = BigDecimal("2.000"),
                price = BigDecimal("10.00"),
                discount = BigDecimal.ZERO,
                totalPrice = BigDecimal("20.00")
            )
        )
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(getBudgetItemsResponse = Response.success(expected))
        )

        val values = viewModel.getBudgetItems(10).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getBudgets_emitsSuccessWithReturnedBudget() {
        val expected = Budget(
            totalSpend = BigDecimal("250.00"),
            totalPlanned = BigDecimal("500.00"),
            totalEarned = BigDecimal("700.00"),
            budgets = listOf(
                MonthBudget(
                    budgetId = 3,
                    category = "Home",
                    spent = BigDecimal("250.00"),
                    planned = BigDecimal("500.00"),
                    percentage = 50
                )
            )
        )
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(getBudgetsResponse = Response.success(expected))
        )

        val values = viewModel.getBudgets().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getBudgets_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(getBudgetsResponse = errorResponse())
        )

        val values = viewModel.getBudgets().awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getBudgetItems_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeBudgetApiHelper(getBudgetItemsResponse = errorResponse())
        )

        val values = viewModel.getBudgetItems(10).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    private fun createViewModel(helper: BudgetApiHelper): BudgetViewModel {
        return BudgetViewModel(BudgetRepository(helper))
    }

    private fun <T> LiveData<Resource<T>>.awaitUntilStatus(target: Status): List<Resource<T>> {
        val values = mutableListOf<Resource<T>>()
        val latch = CountDownLatch(1)
        val observer = object : Observer<Resource<T>> {
            override fun onChanged(value: Resource<T>?) {
                if (value != null) {
                    values += value
                    if (value.status == target) {
                        latch.countDown()
                        removeObserver(this)
                    }
                }
            }
        }

        observeForever(observer)
        val completed = latch.await(2, TimeUnit.SECONDS)
        kotlin.runCatching { removeObserver(observer) }
        assertTrue("Timed out waiting for LiveData emissions", completed)
        return values
    }

    private fun <T> errorResponse(): Response<T> {
        return Response.error(500, ResponseBody.create(null, "{\"error\":\"test\"}"))
    }
}

private class FakeBudgetApiHelper(
    private val getBudgetsResponse: Response<Budget> = Response.success(
        Budget(
            totalSpend = BigDecimal.ZERO,
            totalPlanned = BigDecimal.ZERO,
            totalEarned = BigDecimal.ZERO,
            budgets = listOf(
                MonthBudget(
                    budgetId = 1,
                    category = "General",
                    spent = BigDecimal.ZERO,
                    planned = BigDecimal.ZERO,
                    percentage = 0
                )
            )
        )
    ),
    private val updateResponse: Response<UpdateBudgetResponse> = Response.success(
        UpdateBudgetResponse(
            budgetId = 1,
            category = "General",
            spent = BigDecimal.ZERO,
            planned = BigDecimal.ZERO,
            monthPlanned = BigDecimal.ZERO,
            percentage = 0
        )
    ),
    private val recalculateResponse: Response<Budget> = Response.success(
        Budget(
            totalSpend = BigDecimal.ZERO,
            totalPlanned = BigDecimal.ZERO,
            totalEarned = BigDecimal.ZERO,
            budgets = listOf(
                MonthBudget(
                    budgetId = 1,
                    category = "General",
                    spent = BigDecimal.ZERO,
                    planned = BigDecimal.ZERO,
                    percentage = 0
                )
            )
        )
    ),
    private val getBudgetItemsResponse: Response<List<InvoiceDetails>> = Response.success(emptyList())
) : BudgetApiHelper {

    override suspend fun getBudgets(): Response<Budget> = getBudgetsResponse

    override suspend fun updateBudget(updateBudgetRequest: UpdateBudgetRequest): Response<UpdateBudgetResponse> = updateResponse

    override suspend fun recalculateBudgets(): Response<Budget> = recalculateResponse

    override suspend fun getBudgetItems(budgetId: Int): Response<List<InvoiceDetails>> = getBudgetItemsResponse
}
