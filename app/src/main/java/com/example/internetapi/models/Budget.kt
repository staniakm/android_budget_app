package com.example.internetapi.models

import java.math.BigDecimal

data class Budget(
    val totalSpend: BigDecimal,
    val totalPlanned: BigDecimal,
    val totalEarned: BigDecimal,
    val date: String = "",
    val budgets: List<MonthBudget> = listOf()
)

data class MonthBudget(
    val category: String,
    val spent: BigDecimal,
    val planned: BigDecimal,
    val percentage: Int
)
