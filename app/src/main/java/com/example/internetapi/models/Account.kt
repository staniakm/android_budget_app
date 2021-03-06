package com.example.internetapi.models

import java.io.Serializable
import java.math.BigDecimal
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Account(
    val id: Int,
    val name: String,
    val moneyAmount: BigDecimal,
    val expense: BigDecimal,
    val income: BigDecimal
) : Serializable {

    fun toSimpleAccount() = SimpleAccount(id, name)
}

data class SimpleAccount(val id: Int, val name: String) {
    override fun toString(): String {
        return name
    }
}

data class AccountInvoice(
    val listId: Long,
    val name: String,
    val date: String,
    val price: BigDecimal,
    val account: String
)

data class AccountIncome(
    val id: Long,
    val accountName: String,
    val income: BigDecimal,
    val date: String,
    val description: String
)

data class AccountIncomeRequest(
    val accountId: Int,
    val value: BigDecimal,
    val date: String,
    val incomeDescription: String
)

data class IncomeType(val id: Int, val name: String)

fun LocalDate.format() = DateTimeFormatter.ofPattern("dd-MM-yyyy").format(this)