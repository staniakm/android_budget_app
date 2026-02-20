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

    @Test
    fun transferMoney_emitsSuccessWithUpdatedAccount() {
        val request = TransferMoneyRequest(
            accountId = 1,
            value = BigDecimal("50.00"),
            targetAccount = 2
        )
        val expected = UpdateAccountResponse(1, "Main", BigDecimal("150.00"))
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(transferResponse = Response.success(expected))
        )

        val values = viewModel.transferMoney(request).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun addIncome_emitsErrorWhenApiFails() {
        val request = AccountIncomeRequest(
            accountId = 1,
            value = BigDecimal("100.00"),
            date = "2026-02-16",
            incomeDescription = "Salary"
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(addIncomeResponse = errorResponse())
        )

        val values = viewModel.addIncome(request).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getAccounts_emitsSuccessWithReturnedAccounts() {
        val expected = listOf(
            Account(1, "Main", BigDecimal("200.00"), BigDecimal("50.00"), BigDecimal("40.00")),
            Account(2, "Savings", BigDecimal("800.00"), BigDecimal.ZERO, BigDecimal("100.00"))
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(getAccountsResponse = Response.success(expected))
        )

        val values = viewModel.getAccounts().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun accountInvoices_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(getAccountInvoicesResponse = errorResponse())
        )

        val values = viewModel.accountInvoices(1).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getAccountIncome_emitsSuccessWithReturnedIncomeList() {
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
            helper = FakeAccountApiHelper(getAccountIncomeResponse = Response.success(expected))
        )

        val values = viewModel.getAccountIncome(1).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun updateAccount_emitsSuccessWithUpdatedResponse() {
        val request = UpdateAccountRequest(
            id = 1,
            name = "Main",
            newMoneyAmount = BigDecimal("250.00")
        )
        val expected = UpdateAccountResponse(1, "Main", BigDecimal("250.00"))
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(updateAccountResponse = Response.success(expected))
        )

        val values = viewModel.updateAccount(1, request).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun updateAccount_emitsErrorWhenApiFails() {
        val request = UpdateAccountRequest(
            id = 1,
            name = "Main",
            newMoneyAmount = BigDecimal("250.00")
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(updateAccountResponse = errorResponse())
        )

        val values = viewModel.updateAccount(1, request).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getIncomeTypes_emitsSuccessWithReturnedTypes() {
        val expected = listOf(
            IncomeType(id = 1, name = "Salary"),
            IncomeType(id = 2, name = "Bonus")
        )
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(getIncomeTypesResponse = Response.success(expected))
        )

        val values = viewModel.getIncomeTypes().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getOperations_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeAccountApiHelper(getOperationsResponse = errorResponse())
        )

        val values = viewModel.getOperations(1).awaitUntilStatus(Status.ERROR)

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
        kotlin.runCatching { removeObserver(observer) }
        assertTrue("Timed out waiting for LiveData emission", completed)
        return values
    }

    private fun <T> errorResponse(): Response<T> {
        val body: ResponseBody = ResponseBody.create(null, "{\"error\":\"test\"}")
        return Response.error(500, body)
    }
}

private class FakeAccountApiHelper(
    private val getAccountsResponse: Response<List<Account>> = Response.success(emptyList()),
    private val getAccountInvoicesResponse: Response<List<AccountInvoice>> = Response.success(emptyList()),
    private val getAccountIncomeResponse: Response<List<AccountIncome>> = Response.success(emptyList()),
    private val addIncomeResponse: Response<List<AccountIncome>> = Response.success(emptyList()),
    private val transferResponse: Response<UpdateAccountResponse> = Response.success(
        UpdateAccountResponse(1, "Main", BigDecimal("200.00"))
    ),
    private val updateAccountResponse: Response<UpdateAccountResponse> = Response.success(
        UpdateAccountResponse(1, "Main", BigDecimal("200.00"))
    ),
    private val getIncomeTypesResponse: Response<List<IncomeType>> = Response.success(emptyList()),
    private val getOperationsResponse: Response<List<AccountOperation>> = Response.success(emptyList())
) : AccountApiHelper {

    override suspend fun getAccounts(): Response<List<Account>> = getAccountsResponse

    override suspend fun getAccountInvoices(accountId: Int): Response<List<AccountInvoice>> = getAccountInvoicesResponse

    override suspend fun getAccountIncome(accountId: Int): Response<List<AccountIncome>> = getAccountIncomeResponse

    override suspend fun addAccountIncome(request: AccountIncomeRequest): Response<List<AccountIncome>> = addIncomeResponse

    override suspend fun transferMoney(request: TransferMoneyRequest): Response<UpdateAccountResponse> = transferResponse

    override suspend fun updateAccount(accountId: Int, updateAccountRequest: UpdateAccountRequest): Response<UpdateAccountResponse> =
        updateAccountResponse

    override suspend fun getIncomeTypes(): Response<List<IncomeType>> = getIncomeTypesResponse

    override suspend fun getAccountOperations(accountId: Int): Response<List<AccountOperation>> = getOperationsResponse
}
