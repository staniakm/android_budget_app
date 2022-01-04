package com.example.internetapi.models

import java.time.LocalDate
import java.util.*

class Invoice(val accountId: Int) {
    var date: LocalDate? = null
    var shop: Shop? = null


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