package com.example.internetapi.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class InvoiceModelTest {

    @Test
    fun setShop_createsUppercaseShopWhenMissing() {
        val invoice = Invoice(accountId = 10)

        invoice.setShop("local market")

        assertEquals(-1, invoice.shop?.shopId)
        assertEquals("LOCAL MARKET", invoice.shop?.name)
    }

    @Test
    fun isBasicDataNotFilled_returnsFalseWhenShopPresent() {
        val invoice = Invoice(accountId = 10)
        invoice.shop = Shop(1, "Shop")

        assertFalse(invoice.isBasicDataNotFilled())
    }

    @Test
    fun invoiceItem_totalPriceAndRequestSum_areCalculatedCorrectly() {
        val itemA = InvoiceItem(
            shopItem = ShopItem(1, "Milk"),
            price = BigDecimal("10.00"),
            amount = BigDecimal("2.000"),
            discount = BigDecimal("1.50")
        )
        val itemB = InvoiceItem(
            shopItem = ShopItem(2, "Bread"),
            price = BigDecimal("5.00"),
            amount = BigDecimal("1.000"),
            discount = BigDecimal.ZERO
        )

        val request = NewInvoiceRequest(
            accountId = 3,
            shopId = 7,
            date = "2026-02-16",
            items = listOf(itemA.toNewInvoiceItemRequest(), itemB.toNewInvoiceItemRequest())
        )

        assertEquals(0, itemA.totalPrice().compareTo(BigDecimal("18.50")))
        assertEquals(0, request.sum.compareTo(BigDecimal("23.50")))
    }

    @Test
    fun invoiceItem_isNewShopItem_detectsTemporaryItem() {
        val newItem = InvoiceItem(
            shopItem = ShopItem(-1, "Custom item"),
            price = BigDecimal.ONE,
            amount = BigDecimal.ONE
        )

        assertTrue(newItem.isNewShopItem())
    }
}
