package com.example.internetapi.ui

import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem

fun filterShopsForAutocomplete(query: String, shops: List<Shop>): List<Shop> {
    val text = query.trim()
    if (text.isEmpty()) return emptyList()
    return shops.filter { it.name.contains(text, ignoreCase = true) }
}

fun filterShopItemsForAutocomplete(query: String, items: List<ShopItem>): List<ShopItem> {
    val text = query.trim()
    if (text.isEmpty()) return emptyList()
    return items.filter { it.name.contains(text, ignoreCase = true) }
}

fun shouldShowAutocompleteSuggestions(hasFocus: Boolean, suggestionsCount: Int): Boolean {
    return hasFocus && suggestionsCount > 0
}
