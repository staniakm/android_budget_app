package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.InvoiceApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.api.ShopApiHelper
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.CreateInvoiceResponse
import com.example.internetapi.models.CreateShopItemRequest
import com.example.internetapi.models.CreateShopRequest
import com.example.internetapi.models.InvoiceDetails
import com.example.internetapi.models.NewInvoiceItemRequest
import com.example.internetapi.models.NewInvoiceRequest
import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem
import com.example.internetapi.models.Status
import com.example.internetapi.models.UpdateInvoiceAccountRequest
import com.example.internetapi.repository.InvoiceRepository
import com.example.internetapi.repository.ShopRepository
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
class AccountOutcomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("UnitTestMain")

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
    fun getShops_emitsLoadingThenSuccess() {
        val shops = listOf(Shop(1, "A"), Shop(2, "B"))
        val viewModel = createViewModel(
            shopHelper = FakeShopApiHelper(getShopsResponse = Response.success(shops))
        )

        val values = viewModel.getShops().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(shops, values.last().data)
    }

    @Test
    fun createShop_emitsSuccessWithCreatedShop() {
        val created = Shop(10, "NEW SHOP")
        val viewModel = createViewModel(
            shopHelper = FakeShopApiHelper(createShopResponse = Response.success(created))
        )

        val values = viewModel.createShop("new shop").awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(created, values.last().data)
    }

    @Test
    fun getShopItems_emitsErrorWhenRepositoryFails() {
        val viewModel = createViewModel(
            shopHelper = FakeShopApiHelper(getShopItemsResponse = errorResponse())
        )

        val values = viewModel.getShopItems(1).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun createNewInvoice_emitsSuccessWithCalculatedSum() {
        val request = NewInvoiceRequest(
            accountId = 1,
            shopId = 2,
            date = "2026-02-16",
            items = listOf(
                NewInvoiceItemRequest(ShopItem(1, "A"), BigDecimal("10.00"), BigDecimal("2.000"), BigDecimal("1.00")),
                NewInvoiceItemRequest(ShopItem(2, "B"), BigDecimal("5.00"), BigDecimal("1.000"), BigDecimal.ZERO)
            )
        )
        val response = CreateInvoiceResponse(
            id = 7,
            date = "2026-02-16",
            invoiceNumber = "INV-7",
            sum = BigDecimal("24.00"),
            description = "",
            account = 1,
            shop = 2
        )
        val viewModel = createViewModel(
            invoiceHelper = FakeInvoiceApiHelper(createInvoiceResponse = Response.success(response))
        )

        val values = viewModel.createNewInvoice(request).awaitUntilStatus(Status.SUCCESS)

        assertEquals(0, request.sum.compareTo(BigDecimal("24.00")))
        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(response, values.last().data)
    }

    private fun createViewModel(
        shopHelper: ShopApiHelper = FakeShopApiHelper(),
        invoiceHelper: InvoiceApiHelper = FakeInvoiceApiHelper(),
    ): AccountOutcomeViewModel {
        return AccountOutcomeViewModel(
            shopRepository = ShopRepository(shopHelper),
            invoiceRepository = InvoiceRepository(invoiceHelper)
        )
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
        val body: ResponseBody = ResponseBody.create(null, "{\"error\":\"test\"}")
        return Response.error(500, body)
    }
}

private class FakeShopApiHelper(
    private val getShopsResponse: Response<List<Shop>> = Response.success(emptyList()),
    private val getShopItemsResponse: Response<List<ShopItem>> = Response.success(emptyList()),
    private val createShopResponse: Response<Shop> = Response.success(Shop(1, "SHOP")),
    private val createShopItemResponse: Response<ShopItem> = Response.success(ShopItem(1, "ITEM")),
) : ShopApiHelper {
    override suspend fun getShops(): Response<List<Shop>> = getShopsResponse

    override suspend fun getShopItems(shopId: Int): Response<List<ShopItem>> = getShopItemsResponse

    override suspend fun createShop(shopRequest: CreateShopRequest): Response<Shop> = createShopResponse

    override suspend fun createNewShopItem(createShopItemRequest: CreateShopItemRequest): Response<ShopItem> =
        createShopItemResponse
}

private class FakeInvoiceApiHelper(
    private val createInvoiceResponse: Response<CreateInvoiceResponse> = Response.success(
        CreateInvoiceResponse(1, "2026-01-01", "INV-1", BigDecimal.ZERO, "", 1, 1)
    )
) : InvoiceApiHelper {
    override suspend fun getInvoiceDetails(invoiceId: Long): Response<List<InvoiceDetails>> =
        Response.success(emptyList())

    override suspend fun updateInvoiceAccount(updateInvoiceAccountRequest: UpdateInvoiceAccountRequest): Response<AccountInvoice> =
        errorResponse()

    override suspend fun createNewInvoice(newInvoiceRequest: NewInvoiceRequest): Response<CreateInvoiceResponse> =
        createInvoiceResponse

    override suspend fun deleteInvoice(invoiceId: Long): Response<Void> = errorResponse()

    private fun <T> errorResponse(): Response<T> {
        val body: ResponseBody = ResponseBody.create(null, "{\"error\":\"not implemented\"}")
        return Response.error(500, body)
    }
}
