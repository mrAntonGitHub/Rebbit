package com.larin_anton.rebbit.adapters

import androidx.recyclerview.widget.DiffUtil
import com.larin_anton.rebbit.data.model.RemotePost

class ItemsDiffCallback(var oldList: List<RemotePost>, var newList: List<RemotePost>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (oldList[oldItemPosition].postId == newList[newItemPosition].postId)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].equals(newList[newItemPosition])
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}