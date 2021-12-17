package com.example.internetapi.models

import java.math.BigDecimal

data class UpdateBudgetRequest(var category: String, var planned: BigDecimal)
