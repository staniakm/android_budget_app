package com.example.internetapi.constant

object Constant {
    const val DEFAULT_BASE_URL: String = "http://192.168.0.11:8080/api/"
    var BASE_URL: String = DEFAULT_BASE_URL
    const val ACCOUNT ="account"
    const val BUDGET = "budget"
    const val INVOICE = "invoice"
    const val SHOP = "shop"

    fun normalizeBaseUrl(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return null

        val withScheme = when {
            trimmed.startsWith("http://", ignoreCase = true) -> trimmed
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "http://$trimmed"
        }

        return if (withScheme.endsWith('/')) withScheme else "$withScheme/"
    }
}
