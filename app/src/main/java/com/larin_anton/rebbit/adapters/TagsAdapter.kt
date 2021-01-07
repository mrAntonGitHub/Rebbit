package com.larin_anton.rebbit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.Tag

// TODO Refactoring REQUIRE
class TagsAdapter(private val postTags: List<Tag>, val onTagAdapterAction: OnTagAdapterAction) : RecyclerView.Adapter<TagsAdapter.ViewHolder>() {

    interface OnTagAdapterAction {
        fun onTagClicked(postTag: Tag)
    }

    override fun getItemCount(): Int {
        return postTags.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            tagName.text = postTags[position].name
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tagName: TextView = view.findViewById(R.id.tagNameTextView)

        init {
            tagName.setOnClickListener {
                onTagAdapterAction.onTagClicked(postTags[adapterPosition])
            }
        }
    }
}