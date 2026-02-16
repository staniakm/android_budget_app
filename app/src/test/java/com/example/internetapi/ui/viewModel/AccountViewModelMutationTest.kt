package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.AccountApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Account
import com.example.internetapi.models.AccountIncome
import com.example.internetapi.models.AccountIncomeRequest
import com.example.internetapi.models.AccountInvoice
import com.example.internetapi.models.AccountOperation
import com.example.internetapi.models.IncomeType
import com.example.internetapi.models.Status
import com.example.internetapi.models.TransferMoneyRequest
import com.example.internetapi.models.UpdateAccountRequest
import com.example.internetapi.models.UpdateAccountResponse
import com.example.internetapi.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelMutationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("AccountMutationTestMain")

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
    fun addIncome_emitsSuccessWithReturnedIncomeList() {
        val request = AccountIncomeRequest(
            accountId = 1,
            value = BigDecimal("100.00"),
            date = "2026-02-16",
            incomeDescription = "Salary"
        )
        val expected = listOf(
            AccountIncome(
                id = 10,
                accountName = "Main",
                income = BigDecimal("100.00"),
                date = "2026-02-16",
                description = "Salary"
            )
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(addIncomeResponse = Response.success(expected))
        )

        val values = viewModel.addIncome(request).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun transferMoney_emitsErrorWhenApiFails() {
        val request = TransferMoneyRequest(
            accountId = 1,
            value = BigDecimal("50.00"),
            targetAccount = 2
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(transferResponse = errorResponse())
        )

        val values = viewModel.transferMoney(request).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    private fun createViewModel(helper: AccountApiHelper): AccountViewModel {
        return AccountViewModel(AccountRepository(helper))
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
        removeObserver(observer)
        assertTrue("Timed out waiting for LiveData emission", completed)
        return values
    }

    private fun <T> errorResponse(): Response<T> {
        val body: ResponseBody = ResponseBody.create(null, "{\"error\":\"test\"}")
        return Response.error(500, body)
    }
}

private class FakeAccountApiHelper(
    private val addIncomeResponse: Response<List<AccountIncome>> = Response.success(emptyList()),
    private val transferResponse: Response<UpdateAccountResponse> = Response.success(
        UpdateAccountResponse(1, "Main", BigDecimal("200.00"))
    )
) : AccountApiHelper {

    override suspend fun getAccounts(): Response<List<Account>> = Response.success(emptyList())

    override suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>> = Response.success(emptyList())

    override suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>> = Response.success(emptyList())

    override suspend fun addAccountIncome(request: AccountIncomeRequest): Response<List<AccountIncome>> = addIncomeResponse

    override suspend fun transferMoney(request: TransferMoneyRequest): Response<UpdateAccountResponse> = transferResponse

    override suspend fun updateAccount(accountId: Int, updateAccountRequest: UpdateAccountRequest): Response<UpdateAccountResponse> =
        Response.success(UpdateAccountResponse(accountId.toLong(), updateAccountRequest.name, updateAccountRequest.newMoneyAmount))

    override suspend fun getIncomeTypes(): Response<List<IncomeType>> = Response.success(emptyList())

    override suspend fun getAccountOperations(accountId: Int): Response<List<AccountOperation>> = Response.success(emptyList())
}
