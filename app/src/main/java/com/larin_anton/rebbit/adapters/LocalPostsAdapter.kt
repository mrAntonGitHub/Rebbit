package com.larin_anton.rebbit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.LocalPost
import com.larin_anton.rebbit.data.model.Tag

// TODO Refactoring REQUIRE
class LocalPostsAdapter (private val localPosts: List<LocalPost>, val onPostAdapterAction: OnPostAdapterAction) : RecyclerView.Adapter<LocalPostsAdapter.ViewHolder>(), TagsAdapter.OnTagAdapterAction {

        interface OnPostAdapterAction {
            fun onDeleteClicked(localPost: LocalPost)
            fun onPostClicked(postId: Long)
        }

        override fun getItemCount(): Int {
            return localPosts.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_private_post, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (localPosts[position].listOfPostTags.isNullOrEmpty()) {
                holder.tagsRecyclerView.visibility = View.GONE
            }

            else{
                holder.tagsRecyclerView.visibility = View.VISIBLE
                val linearLayoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
                holder.tagsRecyclerView.layoutManager = linearLayoutManager
                holder.tagsRecyclerView.adapter = localPosts[position].listOfPostTags.let { TagsAdapter(it.toList(), this) }
            }

            holder.apply {
                status.text = localPosts[position].status
                publishedTime.text = localPosts[position].published_time
                body.text = localPosts[position].body
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val status: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_status)
            val publishedTime: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_publishedTimeTextView)
            val body: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_bodyTextView)
            val tagsRecyclerView: RecyclerView = view.findViewById(R.id.itemPrivateNotPublishedPost_tagsRecyclerView)
            private val moreButton: Button = view.findViewById(R.id.itemPrivateNotPublishedPost_moreButton)



            init {
                moreButton.setOnClickListener {
                    val popupMenu = PopupMenu(it.context, it)
                    popupMenu.menuInflater.inflate(R.menu.menu_private_posts, popupMenu.menu)

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        when (menuItem?.itemId) {
                            R.id.post_menu_delete -> {
                                Toast.makeText(it.context, "Delete", Toast.LENGTH_SHORT).show()
                            }
                            R.id.post_menu_edit -> {
                                Toast.makeText(it.context, "Change", Toast.LENGTH_SHORT).show()
                            }
                            R.id.post_menu_publish -> {
                                Toast.makeText(it.context, "Publish", Toast.LENGTH_SHORT).show()
                            }
                        }
                        false
                    }

                    popupMenu.show()
                }
                view.setOnClickListener {
                    val postId = localPosts[adapterPosition].postId
                    if (postId != null) {
                        onPostAdapterAction.onPostClicked(postId)
                    }
                }
            }
        }

        private fun timeToRead(text: String, context: Context): String{
            return when (val time = countTimeToRead(text)){
                0 -> "Займет менее минуты"
                else -> {
                    context.resources.getQuantityString(R.plurals.time, time, time)
                }
            }
        }

        private fun countTimeToRead(text: String): Int{
            val countOfWords =  text.trim().split("\\s+".toRegex()).size
            return countOfWords / 150
        }

    override fun onTagClicked(postTag: Tag) {
        TODO("Not yet implemented")
    }

}