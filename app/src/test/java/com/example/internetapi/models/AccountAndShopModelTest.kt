package com.example.internetapi.models

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class AccountAndShopModelTest {

    @Test
    fun account_toSimpleAccount_mapsIdAndName() {
        val account = Account(
            id = 7,
            name = "Main",
            moneyAmount = BigDecimal("100.00"),
            expense = BigDecimal("20.00"),
            income = BigDecimal("10.00")
        )

        val result = account.toSimpleAccount()

        assertEquals(7, result.id)
        assertEquals("Main", result.name)
    }

    @Test
    fun simpleAccount_toString_returnsName() {
        val simple = SimpleAccount(id = 3, name = "Savings")

        assertEquals("Savings", simple.toString())
    }

    @Test
    fun shopAndShopItem_toString_returnsName() {
        val shop = Shop(shopId = 1, name = "Market")
        val item = ShopItem(itemId = 2, name = "Milk")

        assertEquals("Market", shop.toString())
        assertEquals("Milk", item.toString())
    }

    @Test
    fun localDate_format_returnsExpectedPattern() {
        val date = LocalDate.of(2026, 2, 5)

        val result = date.format()

        assertEquals("05-02-2026", result)
    }
}
