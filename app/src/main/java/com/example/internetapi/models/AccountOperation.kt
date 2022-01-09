package com.example.internetapi.models

import java.math.BigDecimal

data class AccountOperation(
    val id: Long,
    val date: String,
    val value: BigDecimal,
    val account: Int,
    val type: String
)
