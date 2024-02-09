package com.example.internetapi.config

import java.text.DecimalFormat

object MoneyFormatter {

    val df: DecimalFormat = DecimalFormat("##0.00")
    val dfInt: DecimalFormat = DecimalFormat("##0")
}

object AmountFormatter {

    val df: DecimalFormat = DecimalFormat("##0.000")
}