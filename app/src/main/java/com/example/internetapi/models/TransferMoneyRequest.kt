package com.example.internetapi.models

import java.math.BigDecimal

data class TransferMoneyRequest(val accountId: Int, val value: BigDecimal, val targetAccount: Int)