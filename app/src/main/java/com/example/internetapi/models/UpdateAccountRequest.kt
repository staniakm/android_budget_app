package com.example.internetapi.models

import java.io.Serializable
import java.math.BigDecimal

data class UpdateAccountRequest (val id: Long, val name: String, val newMoneyAmount: BigDecimal):Serializable
data class UpdateAccountResponse (val id: Long, val name: String, val amount: BigDecimal):Serializable
