package com.example.internetapi.models

import java.math.BigDecimal

data class InvoiceDetails(
    val invoiceItemId: Int,
    val productName: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val discount: BigDecimal,
    val totalPrice: BigDecimal,
)
