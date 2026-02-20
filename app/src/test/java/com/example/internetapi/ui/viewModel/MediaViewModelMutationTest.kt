package com.example.internetapi.ui.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.MediaApiHelper
import com.example.internetapi.api.Resource
import com.example.internetapi.models.MediaRegisterRequest
import com.example.internetapi.models.MediaType
import com.example.internetapi.models.MediaTypeRequest
import com.example.internetapi.models.MediaUsage
import com.example.internetapi.models.Status
import com.example.internetapi.repository.MediaRepository
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
class MediaViewModelMutationTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("MediaMutationTestMain")

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
    fun addNewMediaType_emitsSuccessWithReturnedType() {
        val expected = MediaType(7, "Water")
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(addTypeResponse = Response.success(expected))
        )

        val values = viewModel.addNewMediaType(
            MediaTypeRequest(mediaName = "Water")
        ).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun addMediaUsageEntry_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(addUsageResponse = errorResponse())
        )

        val values = viewModel.addMediaUsageEntry(
            MediaRegisterRequest(mediaType = 2, meterRead = BigDecimal("321.0"), year = 2026, month = 2)
        ).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getMediaTypes_emitsSuccessWithReturnedTypes() {
        val expected = listOf(
            MediaType(id = 1, name = "Water"),
            MediaType(id = 2, name = "Gas")
        )
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(getTypesResponse = Response.success(expected))
        )

        val values = viewModel.getMediaTypes().awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getMediaTypes_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(getTypesResponse = errorResponse())
        )

        val values = viewModel.getMediaTypes().awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun getMediaUsageByType_emitsSuccessWithReturnedUsageList() {
        val expected = listOf(
            MediaUsage(id = 1, year = 2026, month = 1, meterRead = BigDecimal("300.0")),
            MediaUsage(id = 2, year = 2026, month = 2, meterRead = BigDecimal("320.0"))
        )
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(getUsageResponse = Response.success(expected))
        )

        val values = viewModel.getMediaUsageByType(1).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun getMediaUsageByType_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(getUsageResponse = errorResponse())
        )

        val values = viewModel.getMediaUsageByType(1).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun addNewMediaType_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(addTypeResponse = errorResponse())
        )

        val values = viewModel.addNewMediaType(
            MediaTypeRequest(mediaName = "Gas")
        ).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    @Test
    fun removeMediaUsage_emitsSuccessOnValidResponse() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(removeUsageResponse = Response.success(null))
        )

        val values = viewModel.removeMediaUsage(9).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
    }

    @Test
    fun addMediaUsageEntry_emitsSuccessWithUsageList() {
        val expected = listOf(
            MediaUsage(id = 1, year = 2026, month = 2, meterRead = BigDecimal("322.0"))
        )
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(addUsageResponse = Response.success(expected))
        )

        val values = viewModel.addMediaUsageEntry(
            MediaRegisterRequest(mediaType = 2, meterRead = BigDecimal("322.0"), year = 2026, month = 2)
        ).awaitUntilStatus(Status.SUCCESS)

        assertEquals(Status.SUCCESS, values.last().status)
        assertEquals(expected, values.last().data)
    }

    @Test
    fun removeMediaUsage_emitsErrorWhenApiFails() {
        val viewModel = createViewModel(
            helper = FakeMediaApiHelper(removeUsageResponse = errorResponse())
        )

        val values = viewModel.removeMediaUsage(9).awaitUntilStatus(Status.ERROR)

        assertEquals(Status.ERROR, values.last().status)
    }

    private fun createViewModel(helper: MediaApiHelper): MediaViewModel {
        return MediaViewModel(MediaRepository(helper))
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

private class FakeMediaApiHelper(
    private val getTypesResponse: Response<List<MediaType>> = Response.success(emptyList()),
    private val addTypeResponse: Response<MediaType> = Response.success(MediaType(1, "Water")),
    private val getUsageResponse: Response<List<MediaUsage>> = Response.success(emptyList()),
    private val addUsageResponse: Response<List<MediaUsage>> = Response.success(emptyList()),
    private val removeUsageResponse: Response<Void> = Response.success(null)
) : MediaApiHelper {

    override suspend fun getMediaTypes(): Response<List<MediaType>> = getTypesResponse

    override suspend fun addNewMediaType(mediaTypeRequest: MediaTypeRequest): Response<MediaType> = addTypeResponse

    override suspend fun getMediaUsageByType(mediaTypeId: Int): Response<List<MediaUsage>> = getUsageResponse

    override suspend fun addMediaUsageEntry(mediaUsageRequest: MediaRegisterRequest): Response<List<MediaUsage>> = addUsageResponse

    override suspend fun removeMediaUsageItem(id: Int): Response<Void> = removeUsageResponse
}
