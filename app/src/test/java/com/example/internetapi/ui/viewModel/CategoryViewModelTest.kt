package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.CategoryApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Category
import com.example.internetapi.models.CategoryDetails
import com.example.internetapi.models.Status
import com.example.internetapi.repository.CategoryRepository
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
class CategoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("CategoryViewModelTestMain")

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
    fun getCategories_emitsSuccessWithReturnedList() {
        val expected = listOf(
            Category(1, "Food", BigDecimal("120.00"), BigDecimal("450.00")),
            Category(2, "Transport", BigDecimal("60.00"), BigDecimal("200.00"))
        )
        val viewModel = createViewModel(
            helper = FakeCategoryApiHelper(getCategoriesResponse = Response.success(expected))
        )

        val values = viewModel.getCategories().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getCategories_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeCategoryApiHelper(getCategoriesResponse = errorResponse())
        )

        val values = viewModel.getCategories().awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getCategoryDetails_emitsSuccessWithReturnedDetails() {
        val expected = listOf(
            CategoryDetails(assortmentId = 10, name = "Milk", price = BigDecimal("8.50")),
            CategoryDetails(assortmentId = 11, name = "Bread", price = BigDecimal("4.20"))
        )
        val viewModel = createViewModel(
            helper = FakeCategoryApiHelper(getCategoryDetailsResponse = Response.success(expected))
        )

        val values = viewModel.getCategoryDetails(1).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getCategoryDetails_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeCategoryApiHelper(getCategoryDetailsResponse = errorResponse())
        )

        val values = viewModel.getCategoryDetails(1).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    private fun createViewModel(helper: CategoryApiHelper): CategoryViewModel {
        return CategoryViewModel(CategoryRepository(helper))
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
        assertTrue("Timed out waiting for LiveData emission", completed)
        return values
    }

    private fun <T> errorResponse(): Response<T> {
        return Response.error(500, ResponseBody.create(null, "{\"error\":\"test\"}"))
    }
}

private class FakeCategoryApiHelper(
    private val getCategoriesResponse: Response<List<Category>> = Response.success(emptyList()),
    private val getCategoryDetailsResponse: Response<List<CategoryDetails>> = Response.success(emptyList())
) : CategoryApiHelper {

    override suspend fun getCategories(): Response<List<Category>> = getCategoriesResponse

    override suspend fun getCategoryDetails(categoryId: Int): Response<List<CategoryDetails>> = getCategoryDetailsResponse
}
