package com.example.internetapi.config

import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FormatterTest {

    @Test
    fun moneyFormatter_formatsTwoDecimals() {
        val formatted = MoneyFormatter.df.format(BigDecimal("12.5"))

        assertTrue(formatted.matches(Regex("12[.,]50")))
    }

    @Test
    fun amountFormatter_formatsThreeDecimals() {
        val formatted = AmountFormatter.df.format(BigDecimal("7.2"))

        assertTrue(formatted.matches(Regex("7[.,]200")))
    }
}
