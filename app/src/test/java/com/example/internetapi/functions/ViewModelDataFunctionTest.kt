package com.example.internetapi.functions

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.internetapi.api.Resource
import com.example.internetapi.models.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelDataFunctionTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mainThread = newSingleThreadContext("ViewModelDataFunctionTestMain")

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
    fun executeLiveDataList_emitsLoadingThenSuccess() {
        val viewModel = object : ViewModelDataFunction() {}

        val emissions = viewModel.executeLiveDataList {
            Response.success(listOf("A", "B"))
        }.awaitEmissions(2)

        assertEquals(Status.LOADING, emissions[0].status)
        assertNull(emissions[0].data)
        assertEquals(Status.SUCCESS, emissions[1].status)
        assertEquals(listOf("A", "B"), emissions[1].data)
    }

    @Test
    fun executeLiveDataList_emitsLoadingThenErrorWhenResponseFails() {
        val viewModel = object : ViewModelDataFunction() {}

        val emissions = viewModel.executeLiveDataList<String> {
            Response.error(500, ResponseBody.create(null, "boom"))
        }.awaitEmissions(2)

        assertEquals(Status.LOADING, emissions[0].status)
        assertEquals(Status.ERROR, emissions[1].status)
        assertNull(emissions[1].data)
    }

    @Test
    fun executeLiveDataSingle_emitsLoadingThenSuccess() {
        val viewModel = object : ViewModelDataFunction() {}

        val emissions = viewModel.executeLiveDataSingle {
            Response.success("ok")
        }.awaitEmissions(2)

        assertEquals(Status.LOADING, emissions[0].status)
        assertEquals(Status.SUCCESS, emissions[1].status)
        assertEquals("ok", emissions[1].data)
    }

    @Test
    fun executeLiveDataSingle_emitsLoadingThenErrorWhenExceptionThrown() {
        val viewModel = object : ViewModelDataFunction() {}

        val emissions = viewModel.executeLiveDataSingle<String> {
            throw IllegalStateException("network-down")
        }.awaitEmissions(2)

        assertEquals(Status.LOADING, emissions[0].status)
        assertEquals(Status.ERROR, emissions[1].status)
        assertEquals("network-down", emissions[1].message)
    }

    private fun <T> LiveData<Resource<T>>.awaitEmissions(expectedCount: Int): List<Resource<T>> {
        val values = mutableListOf<Resource<T>>()
        val latch = CountDownLatch(1)
        val observer = object : Observer<Resource<T>> {
            override fun onChanged(value: Resource<T>?) {
                if (value != null) {
                    values += value
                    if (values.size >= expectedCount) {
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
}
