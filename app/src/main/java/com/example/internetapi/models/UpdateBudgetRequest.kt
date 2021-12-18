package com.example.internetapi.models

import java.io.Serializable
import java.math.BigDecimal

data class UpdateBudgetRequest(val budgetId: Int, var planned: BigDecimal)
data class UpdateBudgetResponse(
    val budgetId: Int,
    val category: String,
    val spent: BigDecimal,
    val planned: BigDecimal,
    val monthPlanned:BigDecimal,
    val percentage: Int
) : Serializable
