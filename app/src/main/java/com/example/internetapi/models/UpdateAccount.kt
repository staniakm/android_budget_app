package com.example.internetapi.models

import java.math.BigDecimal

data class UpdateAccount (val id: Long, val name: String, val newMoneyAmount: BigDecimal)
