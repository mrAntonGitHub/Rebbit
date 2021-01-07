package com.larin_anton.rebbit.adapters

import com.larin_anton.rebbit.interfaces.PublishedPostsDelegate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.utils.Utils
import com.larin_anton.rebbit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class BookedPostsAdapter : RecyclerView.Adapter<BookedPostsAdapter.ViewHolder>() {

    private var remotePosts: MutableList<RemotePost> = mutableListOf()

    private lateinit var currentUser: User
    private lateinit var bookedDelegate: PublishedPostsDelegate

    fun setDelegate(bookedPostsDelegate: PublishedPostsDelegate){
        this.bookedDelegate = bookedPostsDelegate
    }

    fun setCurrentUser(currentUser: User){
        this.currentUser = currentUser
    }

    override fun getItemCount(): Int {
        return remotePosts.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_booked_post, parent, false), bookedDelegate, currentUser)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.remotePost = remotePosts[position]
    }

    fun submitList(posts: List<RemotePost>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(ItemsDiffCallback(this.remotePosts, posts))
        this.remotePosts = posts.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(val view: View, private val bookedDelegate: PublishedPostsDelegate, val currentUser: User) : RecyclerView.ViewHolder(view),
        TagsAdapter.OnTagAdapterAction {
        private val status: TextView = view.findViewById(R.id.itemPost_status)
        private val author: TextView = view.findViewById(R.id.itemPost_author)
        private val body: TextView = view.findViewById(R.id.itemPost_bodyTextView)
        private val takesTime : TextView = view.findViewById(R.id.itemPost_takesTimeToRead)
        private val expandPost : TextView = view.findViewById(R.id.itemPost_expand)
        private val publishedTime: TextView = view.findViewById(R.id.itemPost_publishedTimeTextView)
        private val authorButton: TextView = view.findViewById(R.id.itemPost_author)

        private val comments: Button = view.findViewById(R.id.itemPost_comments)
        private val likes: Button = view.findViewById(R.id.itemPost_like)
        private val moreButton: Button = view.findViewById(R.id.itemPost_moreButton)
        private val removeFromBookmarks : ImageButton = view.findViewById(R.id.itemPost_remove)

        private val tagsRecyclerView: RecyclerView = view.findViewById(R.id.itemPost_tagsRecyclerView)

        var remotePost: RemotePost = RemotePost()
            set(value){
                field = value

                setButtons(field)
                setTags(field)
                setViewData(field)

            }

        private fun setButtons(field: RemotePost){
            moreButton.setOnClickListener {
                Utils.createPopupMenu(it, field, bookedDelegate)
            }

            likes.setOnClickListener {
                CoroutineScope(IO).launch {
                    bookedDelegate.onLikeClicked(field, !it.isSelected)
                }
            }

            view.setOnClickListener{
                CoroutineScope(IO).launch {
                    bookedDelegate.onCommentClicked(field)
                }
            }

            comments.setOnClickListener {
                CoroutineScope(IO).launch {
                    bookedDelegate.onCommentClicked(field)
                }
            }

            authorButton.setOnClickListener {
                CoroutineScope(IO).launch {
                    bookedDelegate.onAuthorClicked(field.authorId)
                }
            }

            expandPost.setOnClickListener {
                body.maxLines = 10000
                expandPost.visibility = View.GONE
            }

            removeFromBookmarks.setOnClickListener{
                CoroutineScope(IO).launch {
                    bookedDelegate.onAddToBookmarksClicked(field, false)
                }
                removeFromBookmarks.setImageDrawable(ResourcesCompat.getDrawable(it.resources, R.drawable.ic_bookmarks, null ))
            }
        }
        private fun setTags(field: RemotePost){
            val tags = field.listOfTags
            if (tags.isNullOrEmpty()) {
                tagsRecyclerView.visibility = View.GONE
            }
            else{
                tagsRecyclerView.visibility = View.VISIBLE
                val linearLayoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                tagsRecyclerView.layoutManager = linearLayoutManager
                tagsRecyclerView.adapter = TagsAdapter(tags, this)
            }
        }
        private fun setViewData(field: RemotePost){
            apply {
                val postStatus = field.status
                if (!postStatus.isNullOrEmpty()){
                    status.text = postStatus
                    status.visibility = View.VISIBLE
                }
                else{
                    status.visibility = View.GONE
                }

                publishedTime.text = field.published_time
                author.text = field.author
                likes.text = (field.listOfLikedUsersId?.size ?: 0).toString()
                comments.text = (field.listOfComments?.size ?: 0).toString()

                val bodyText = field.body
                if (bodyText.length > 600 && body.maxLines == POST_MAX_LINES){
                    expandPost.visibility = View.VISIBLE
                }else{
                    expandPost.visibility = View.GONE
                }

                body.text = bodyText

                takesTime.text = field.body.let { Utils.estimatedTimeToRead(it, itemView.context) }

                val listOfLikedUser = field.listOfLikedUsersId
                if (listOfLikedUser != null){
                    likes.isSelected = listOfLikedUser.contains(currentUser.id)
                }else{
                    likes.isSelected = false
                }
            }
        }

        override fun onTagClicked(postTag: Tag) {
            TODO("Not yet implemented")
        }
    }
}