package com.example.internetapi.constant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConstantBaseUrlTest {

    @Test
    fun normalizeBaseUrl_returnsNullForBlankValue() {
        val result = Constant.normalizeBaseUrl("   ")

        assertNull(result)
    }

    @Test
    fun normalizeBaseUrl_addsHttpSchemeAndTrailingSlash() {
        val result = Constant.normalizeBaseUrl("192.168.0.11:8080/api")

        assertEquals("http://192.168.0.11:8080/api/", result)
    }

    @Test
    fun normalizeBaseUrl_keepsHttpsAndAddsTrailingSlashWhenMissing() {
        val result = Constant.normalizeBaseUrl("https://example.com/api")

        assertEquals("https://example.com/api/", result)
    }

    @Test
    fun normalizeBaseUrl_keepsHttpValueWithTrailingSlash() {
        val result = Constant.normalizeBaseUrl("http://example.com/api/")

        assertEquals("http://example.com/api/", result)
    }
}
