package com.example.internetapi.models

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class Invoice(val accountId: Int) {
    var date: LocalDate? = null
    var shop: Shop? = null
    var number: String = ""
    var description = ""


    fun setShop(text: String) {
        if (shop == null && text.isNotBlank()) {
            shop = Shop(-1, text.toUpperCase(Locale.ROOT))
        }
    }

    fun isBasicDataNotFilled(): Boolean {
        return shop == null
    }

    override fun toString(): String {
        return "Invoice(date=$date, account=${accountId}, shop=${shop?.shopId})"
    }
}

data class CreateInvoiceResponse(
    val id: Long,
    val date: String,
    val invoiceNumber: String,
    val sum: BigDecimal,
    val description: String,
    val account: Int,
    val shop: Int
)

data class InvoiceItem(
    val shopItem: ShopItem,
    val price: BigDecimal,
    val amount: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isNewShopItem(): Boolean = shopItem.itemId == -1
    fun totalPrice() = price.multiply(amount).minus(discount)
    fun toNewInvoiceItemRequest() = NewInvoiceItemRequest(shopItem, price, amount, discount)
}

data class NewInvoiceRequest(
    val accountId: Int,
    val shopId: Int,
    val date: String,
    val items: List<NewInvoiceItemRequest>,
    val sum: BigDecimal = items.sumOf { it.totalPrice },
    val number: String = "",
    val description: String = ""
)

data class NewInvoiceItemRequest(
    val shopItem: ShopItem, val unitPrice: BigDecimal,
    val amount: BigDecimal, val discount: BigDecimal,
    val totalPrice: BigDecimal = unitPrice.multiply(amount).minus(discount)
)