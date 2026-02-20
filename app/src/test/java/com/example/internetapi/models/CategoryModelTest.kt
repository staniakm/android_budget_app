package com.example.internetapi.models

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CategoryModelTest {

    @Test
    fun category_toString_returnsName() {
        val category = Category(
            id = 1,
            name = "Food",
            monthSummary = BigDecimal("120.00"),
            yearSummary = BigDecimal("450.00")
        )

        assertEquals("Food", category.toString())
    }
}
