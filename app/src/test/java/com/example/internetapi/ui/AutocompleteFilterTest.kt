package com.example.internetapi.ui

import com.example.internetapi.models.Shop
import com.example.internetapi.models.ShopItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutocompleteFilterTest {

    @Test
    fun filterShopsForAutocomplete_returnsMatchingItems_caseInsensitive() {
        val shops = listOf(
            Shop(1, "Market One"),
            Shop(2, "Super Shop"),
            Shop(3, "Bakery")
        )

        val result = filterShopsForAutocomplete("shop", shops)

        assertEquals(1, result.size)
        assertEquals("Super Shop", result.first().name)
    }

    @Test
    fun filterShopsForAutocomplete_returnsEmpty_forBlankQuery() {
        val shops = listOf(Shop(1, "Market One"))

        val result = filterShopsForAutocomplete("   ", shops)

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterShopItemsForAutocomplete_returnsAllMatchingItems() {
        val items = listOf(
            ShopItem(1, "Milk 2%"),
            ShopItem(2, "Milk 3.2%"),
            ShopItem(3, "Bread")
        )

        val result = filterShopItemsForAutocomplete("milk", items)

        assertEquals(2, result.size)
    }

    @Test
    fun shouldShowAutocompleteSuggestions_returnsTrueOnlyWhenFocusedAndHasSuggestions() {
        assertTrue(shouldShowAutocompleteSuggestions(hasFocus = true, suggestionsCount = 1))
        assertTrue(!shouldShowAutocompleteSuggestions(hasFocus = false, suggestionsCount = 1))
        assertTrue(!shouldShowAutocompleteSuggestions(hasFocus = true, suggestionsCount = 0))
    }
}
