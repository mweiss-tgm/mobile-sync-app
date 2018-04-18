package com.example.weiss.firebaseshoppinglist

object Constants {
    @JvmStatic val FIREBASE_ITEM: String = "shop_item"
}

class ToDoItem {
    companion object Factory {
        fun create(): ToDoItem = ToDoItem()
    }
    var objectId: String? = null
    var itemText: String? = null
    var done: Boolean? = false
}