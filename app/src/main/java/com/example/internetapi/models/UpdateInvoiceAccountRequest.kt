package com.example.internetapi.models

data class UpdateInvoiceAccountRequest(val invoiceId: Long, val oldAccount: Int, val newAccount: Int)