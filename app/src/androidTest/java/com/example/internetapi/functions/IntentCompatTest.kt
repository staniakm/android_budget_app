package com.example.internetapi.functions

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.Serializable

@RunWith(AndroidJUnit4::class)
class IntentCompatTest {

    @Test
    fun getSerializableExtraCompat_returnsExpectedObject() {
        val intent = Intent()
        val expected = SampleSerializable("alpha")
        intent.putExtra("sample", expected)

        val result = intent.getSerializableExtraCompat("sample", SampleSerializable::class.java)

        assertEquals(expected.value, result?.value)
    }

    @Test
    fun getSerializableExtraCompat_returnsNullForWrongType() {
        val intent = Intent()
        intent.putExtra("sample", SampleSerializable("alpha"))

        val result = intent.getSerializableExtraCompat("sample", AnotherSerializable::class.java)

        assertNull(result)
    }

    private data class SampleSerializable(val value: String) : Serializable

    private data class AnotherSerializable(val value: String) : Serializable
}
