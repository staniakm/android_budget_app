package com.example.internetapi.models

data class Shop(val shopId: Int, val name: String) {
    override fun toString(): String {
        return name
    }
}

data class ShopItem(val itemId: Int, val name: String)
