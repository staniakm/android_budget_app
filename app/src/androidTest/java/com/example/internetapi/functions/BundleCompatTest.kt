package com.example.internetapi.functions

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class BundleCompatTest {

    @Test
    fun getSerializableCompat_returnsExpectedObject() {
        val bundle = Bundle()
        val expected = SampleSerializable("alpha")
        bundle.putSerializable("sample", expected)

        val result = bundle.getSerializableCompat("sample", SampleSerializable::class.java)

        assertEquals(expected.value, result?.value)
    }

    @Test
    fun getSerializableCompat_returnsNullForWrongType() {
        val bundle = Bundle()
        bundle.putSerializable("sample", SampleSerializable("alpha"))

        val result = bundle.getSerializableCompat("sample", AnotherSerializable::class.java)

        assertNull(result)
    }

    private data class SampleSerializable(val value: String) : Serializable

    private data class AnotherSerializable(val value: String) : Serializable
}
