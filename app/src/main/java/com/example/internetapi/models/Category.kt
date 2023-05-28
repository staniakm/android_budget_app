package com.example.internetapi.models

import java.math.BigDecimal

data class Category(
    val id: Int,
    val name: String,
    val monthSummary: BigDecimal,
    val yearSummary: BigDecimal
)

data class CategoryDetails(val assortmentId: Long, val name: String, val price: BigDecimal)