package com.example.internetapi.functions

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


fun removeRecycleViewItemOnSwipe(recyclerView: RecyclerView, action: (Int) -> Unit) {
    ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            v: RecyclerView,
            h: RecyclerView.ViewHolder,
            t: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(h: RecyclerView.ViewHolder, dir: Int) =
            action(h.absoluteAdapterPosition)

    }).attachToRecyclerView(recyclerView)
}