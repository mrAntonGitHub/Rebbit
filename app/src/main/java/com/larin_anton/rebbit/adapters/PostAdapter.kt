package com.larin_anton.rebbit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.interfaces.PublishedPostsDelegate
import com.larin_anton.rebbit.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

const val POST_MAX_LINES = 15

class PostAdapter : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private lateinit  var remotePostsDelegate: PublishedPostsDelegate

    private var remotePosts: MutableList<RemotePost> = mutableListOf()

    private var user: User? = null

    fun setDelegate(remotePostsDelegate: PublishedPostsDelegate){
        this.remotePostsDelegate = remotePostsDelegate
    }

    fun setUser(user: User?){
        this.user = user
    }

    fun submitList(posts: List<RemotePost>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(ItemsDiffCallback(this.remotePosts, posts))
        this.remotePosts = posts.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }


    override fun getItemCount(): Int {
        return remotePosts.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_published_post, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.post = remotePosts[position]
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view),
        TagsAdapter.OnTagAdapterAction {
        private val status: TextView = view.findViewById(R.id.itemPost_status)
        private val publishedTime: TextView = view.findViewById(R.id.itemPost_publishedTimeTextView)
        private val author: TextView = view.findViewById(R.id.itemPost_author)
        private val body: TextView = view.findViewById(R.id.itemPost_bodyTextView)
        private val takesTime : TextView = view.findViewById(R.id.itemPost_takesTimeToRead)
        private val expandPost : TextView = view.findViewById(R.id.itemPost_expand)
        private val authorButton: TextView = view.findViewById(R.id.itemPost_author)

        private val moreButton: Button = view.findViewById(R.id.itemPost_moreButton)
        private val likes: Button = view.findViewById(R.id.itemPost_like)
        private val comments: Button = view.findViewById(R.id.itemPost_comments)
        private val addToBookmarks : ImageButton = view.findViewById(R.id.itemPost_toBookmarks)

        private val tagsRecyclerView: RecyclerView = view.findViewById(R.id.itemPost_tagsRecyclerView)

        var post: RemotePost = RemotePost()
            set(value) {
                field = value

                setButtons(value)
                setTags(value)
                setViewData(value)
            }

        private fun setButtons(post: RemotePost){
            moreButton.setOnClickListener {
                Utils.createPopupMenu(it, post, remotePostsDelegate)
            }

            likes.setOnClickListener {
                it.isSelected = !it.isSelected
                if (it.isSelected){
                    likes.text = ((post.listOfLikedUsersId?.size?.plus(1)) ?: 1).toString()
                    post.listOfLikedUsersId?.put(user?.id.toString(),user?.name.toString())
                }else {
                    likes.text = ((post.listOfLikedUsersId?.size?.minus(1)) ?: 0).toString()
                    post.listOfLikedUsersId?.remove(user?.id)
                }
                remotePostsDelegate.onLikeClicked(post, it.isSelected)
            }

            view.setOnClickListener{
                CoroutineScope(IO).launch {
                    remotePostsDelegate.onCommentClicked(post)
                }
            }

            comments.setOnClickListener {
                CoroutineScope(IO).launch {
                    remotePostsDelegate.onCommentClicked(post)
                }
            }

            authorButton.setOnClickListener {
                CoroutineScope(IO).launch {
                    remotePostsDelegate.onAuthorClicked(post.authorId)
                }
            }

            expandPost.setOnClickListener {
                body.maxLines = 10000
                expandPost.visibility = View.GONE
            }

            addToBookmarks.setOnClickListener{
                it.isSelected = !it.isSelected
                if (it.isSelected){
                    val userLists = user?.listOfBookmarks
                    if (userLists == null){
                        user?.listOfBookmarks = hashMapOf(post.postId to post)
                    }else{
                        user?.listOfBookmarks?.put(post.postId, post)
                    }
                }else{
                    user?.listOfBookmarks?.remove(post.postId)
                }
                CoroutineScope(IO).launch {
                    remotePostsDelegate.onAddToBookmarksClicked(post, it.isSelected)
                }
            }
        }
        private fun setTags(post: RemotePost){
            if (post.listOfTags.isNullOrEmpty()) {
                tagsRecyclerView.visibility = View.GONE
            }

            else{
                tagsRecyclerView.visibility = View.VISIBLE
                val linearLayoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                tagsRecyclerView.layoutManager = linearLayoutManager
                val tags = post.listOfTags
                tagsRecyclerView.adapter = tags?.let { TagsAdapter(it.toList(), this) }
            }
        }
        private fun setViewData(post: RemotePost){
            apply {
                val postStatus = post.status
                if (!postStatus.isNullOrEmpty()){
                    status.text = postStatus
                    status.visibility = View.VISIBLE
                }
                else{
                    status.visibility = View.GONE
                }

                val bookedPosts = user?.listOfBookmarks
                if (bookedPosts != null && bookedPosts.contains(post.postId)){
                    addToBookmarks.isSelected = true
                }
                publishedTime.text = post.published_time
                author.text = post.author
                likes.text = (post.listOfLikedUsersId?.size ?: 0).toString()
                comments.text = (post.listOfComments?.size ?: 0).toString()

                val bodyText = post.body
                if (bodyText.length > 600 && body.maxLines == POST_MAX_LINES){
                    expandPost.visibility = View.VISIBLE
                }else{
                    expandPost.visibility = View.GONE
                }

                body.text = bodyText

                takesTime.text = post.body.let { Utils.estimatedTimeToRead(it, itemView.context) }

                val listOfLikedUser = post.listOfLikedUsersId
                if (listOfLikedUser != null){
                    likes.isSelected = listOfLikedUser.contains(user?.id)
                }else{
                    likes.isSelected = false
                }
            }
        }
        override fun onTagClicked(postTag: Tag) {

        }
    }

}