package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.InvoiceApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.repository.InvoiceRepository
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
class InvoiceViewModelMutationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("InvoiceMutationTestMain")

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
    fun updateInvoiceAccount_emitsSuccessOnValidResponse() {
        val response = AccountInvoice(
            listId = 99,
            name = "Shop",
            date = "2026-02-16",
            price = BigDecimal("12.00"),
            account = "Main"
        )
        val viewModel = createViewModel(
            helper = FakeInvoiceMutationApiHelper(updateResponse = Response.success(response))
        )

        val values = viewModel.updateInvoiceAccount(
            UpdateInvoiceAccountRequest(invoiceId = 99, oldAccount = 1, newAccount = 2)
        ).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(response, values.last().data)
    }

    @Test
    fun updateInvoiceAccount_emitsErrorOnApiFailure() {
        val viewModel = createViewModel(
            helper = FakeInvoiceMutationApiHelper(updateResponse = errorResponse())
        )

        val values = viewModel.updateInvoiceAccount(
            UpdateInvoiceAccountRequest(invoiceId = 50, oldAccount = 1, newAccount = 3)
        ).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    private fun createViewModel(helper: InvoiceApiHelper): InvoiceViewModel {
        return InvoiceViewModel(InvoiceRepository(helper))
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

private class FakeInvoiceMutationApiHelper(
    private val updateResponse: Response<AccountInvoice> = Response.success(
        AccountInvoice(1, "Shop", "2026-01-01", BigDecimal.ZERO, "Main")
    )
) : InvoiceApiHelper {
    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> {
        return Response.success(emptyList())
    }

    override suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice> {
        return updateResponse
    }

    override suspend fun createNewInvoice(newInvoiceRequest: com.example.internetapi.models.NewInvoiceRequest): Response<com.example.internetapi.models.CreateInvoiceResponse> {
        return Response.error(500, ResponseBody.create(null, "{}"))
    }

    override suspend fun deleteInvoice(invoiceId: Long): Response<Void> {
        return Response.error(500, ResponseBody.create(null, "{}"))
    }
}
