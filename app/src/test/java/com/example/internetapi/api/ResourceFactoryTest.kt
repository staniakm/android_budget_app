package com.example.internetapi.api

import com.example.internetapi.models.Status
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResourceFactoryTest {

    @Test
    fun success_setsSuccessStatusAndCarriesData() {
        val result = Resource.success("ok")

        assertEquals(Status.SUCCESS, result.status)
        assertEquals("ok", result.data)
        assertNull(result.message)
    }

    @Test
    fun error_setsErrorStatusAndMessage() {
        val result = Resource.error("boom", 5)

        assertEquals(Status.ERROR, result.status)
        assertEquals(5, result.data)
        assertEquals("boom", result.message)
    }

    @Test
    fun loading_setsLoadingStatusAndNullMessage() {
        val result = Resource.loading(listOf(1, 2, 3))

        assertEquals(Status.LOADING, result.status)
        assertEquals(listOf(1, 2, 3), result.data)
        assertNull(result.message)
    }
}
