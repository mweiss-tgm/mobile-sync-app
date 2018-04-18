package com.example.weiss.firebaseshoppinglist

object Constants {
    @JvmStatic val FIREBASE_ITEM: String = "shop_item"
}

class ShopItem {
    companion object Factory {
        fun create(): ShopItem = ShopItem()
    }
    var objectId: String? = null
    var itemText: String? = null
    var done: Boolean? = false
}