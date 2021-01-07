package com.larin_anton.rebbit.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.*
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon

private const val privatePosts = 1
private const val onModerationPosts = 2
private const val publishedPosts = 3

private const val POST_IS_HIDED_FOR_OTHER_USERS = "Этот пост виден только Вам"

// TODO Refactoring REQUIRE
class MyPostsAdapter (val listOfPosts: List<MyPosts>, val onMyPostsActions: OnMyPostsActions) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), TagsAdapter.OnTagAdapterAction {

    interface OnMyPostsActions {
        fun onDeleteClicked(localPost: LocalPost)
        fun onPostClicked(postId: Long)
        fun onPublishClicked(localPost: LocalPost)
        fun onEditOnModerationPostClicked(remotePost: RemotePost)
        fun onCancelModerating(remotePost: RemotePost)
        fun onDeletePublishedPostClicked(remotePost: RemotePost)
    }

    override fun getItemCount(): Int {
        return listOfPosts.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(listOfPosts[position].TYPE){
            PostType.PRIVATE -> {
                privatePosts
            }
            PostType.MODERATION -> {
                onModerationPosts
            }
            PostType.PUBLISHED -> {
                publishedPosts
            }
            PostType.ALL -> {
                /*Ignore that case*/
                -1
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            privatePosts -> {
                ViewHolderPrivate(LayoutInflater.from(parent.context).inflate(R.layout.item_private_post, parent, false))
            }
            onModerationPosts -> {
                ViewHolderModeration(LayoutInflater.from(parent.context).inflate(R.layout.item_on_moderation_post, parent, false))
            }
            publishedPosts -> {
                ViewHolderPublished(LayoutInflater.from(parent.context).inflate(R.layout.item_published_post, parent, false))
            }
            else -> {
                Log.e("MyPostsAdapter", "onCreateViewHolder [Error: wrong my posts view type]")
                ViewHolderPrivate(LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType){
            privatePosts -> {
                setLayoutPrivatePosts(holder as MyPostsAdapter.ViewHolderPrivate, position)
            }
            onModerationPosts -> {
                setLayoutModerationPosts(holder as MyPostsAdapter.ViewHolderModeration, position)
            }
            publishedPosts -> {
                setLayoutPublishedPosts(holder as MyPostsAdapter.ViewHolderPublished, position)
            }
            else -> {
                Log.e("MyPostsAdapter", "onBindViewHolder [Undefined itemViewType]")
            }
        }
    }

    //  ViewHolders for different income data
    inner class ViewHolderPrivate(view: View) : RecyclerView.ViewHolder(view){
        val status: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_status)
        val publishedTime: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_publishedTimeTextView)
        val body: TextView = view.findViewById(R.id.itemPrivateNotPublishedPost_bodyTextView)
        val tagsRecyclerView: RecyclerView = view.findViewById(R.id.itemPrivateNotPublishedPost_tagsRecyclerView)
        private val moreButton: Button = view.findViewById(R.id.itemPrivateNotPublishedPost_moreButton)
        private val info : ImageView = view.findViewById(R.id.itemPrivateNotPublishedPost_info)

        init {
            moreButton.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.apply {
                    menuInflater.inflate(R.menu.menu_private_posts, popupMenu.menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem?.itemId) {
                            R.id.post_menu_delete -> {
                                onMyPostsActions.onDeleteClicked(listOfPosts[adapterPosition].post as LocalPost)
                            }
                            R.id.post_menu_edit -> {
                                (listOfPosts[adapterPosition].post as LocalPost).postId.let { it1 ->
                                    onMyPostsActions.onPostClicked(
                                        it1
                                    )
                                }
                            }
                            R.id.post_menu_publish -> {
                                onMyPostsActions.onPublishClicked(listOfPosts[adapterPosition].post as LocalPost)
                            }
                        }
                        false
                    }
                    show()
                }
            }
            view.setOnClickListener {
                (listOfPosts[adapterPosition].post as LocalPost).postId.let { it1 ->
                    onMyPostsActions.onPostClicked(
                        it1
                    )
                }
            }
            info.setOnClickListener {
                showPopUpInfo(it)
            }
        }
        private fun showPopUpInfo(view: View){
            createBalloon(view.context) {
                setArrowSize(5)
                setWidthRatio(0.6f)
                setHeight(65)
                setArrowPosition(0.8f)
                setCornerRadius(4f)
                setAlpha(0.9f)
                setText(POST_IS_HIDED_FOR_OTHER_USERS)
                setTextColorResource(android.R.color.white)
                setBackgroundColorResource(R.color.main_primaryColor)
                onBalloonClickListener?.let { setOnBalloonClickListener(it) }
                setBalloonAnimation(BalloonAnimation.FADE)
                setLifecycleOwner(lifecycleOwner)
            }.showAlignTop(view, -195, 0)
        }
    }

    inner class ViewHolderModeration(view: View) : RecyclerView.ViewHolder(view){
        val status: TextView = view.findViewById(R.id.itemAttemptToPublishPost_status)
        val publishedTime: TextView = view.findViewById(R.id.itemAttemptToPublishPost_publishedTimeTextView)
        val body: TextView = view.findViewById(R.id.itemAttemptToPublishPost_bodyTextView)
        val tagsRecyclerView: RecyclerView = view.findViewById(R.id.itemAttemptToPublishPost_tagsRecyclerView)
        private val moreButton: Button = view.findViewById(R.id.itemAttemptToPublishPost_moreButton)
        private val info : ImageView = view.findViewById(R.id.itemAttemptToPublishPost_info)

        init {
            moreButton.setOnClickListener {
                val popupMenu = PopupMenu(it.context, it)
                popupMenu.apply {
                    menuInflater.inflate(R.menu.menu_on_moderation_posts, popupMenu.menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem?.itemId) {
                            R.id.post_menu_moderation_delete -> {
                                onMyPostsActions.onCancelModerating(listOfPosts[adapterPosition].post as RemotePost)
                            }
                            R.id.post_menu_moderation_edit-> {
                                onMyPostsActions.onEditOnModerationPostClicked(listOfPosts[adapterPosition].post as RemotePost)
                            }
                        }
                        false
                    }
                    show()
                }
            }
            view.setOnClickListener {
                onMyPostsActions.onEditOnModerationPostClicked(listOfPosts[adapterPosition].post as RemotePost)
            }
            info.setOnClickListener {
                showPopUpInfo(it)
            }
        }
        private fun showPopUpInfo(view: View){
            createBalloon(view.context) {
                setArrowSize(5)
                setWidthRatio(0.6f)
                setHeight(65)
                setArrowPosition(0.8f)
                setCornerRadius(4f)
                setAlpha(0.9f)
                setText("В ближайшее время наши модераторы проверят Ваш пост и уведомят Вас о результате")
                setTextColorResource(android.R.color.white)
                setBackgroundColorResource(R.color.orange)
                onBalloonClickListener?.let { setOnBalloonClickListener(it) }
                setBalloonAnimation(BalloonAnimation.FADE)
                setLifecycleOwner(lifecycleOwner)
            }.showAlignTop(view, -195, 0)
        }
    }

    inner class ViewHolderPublished(view: View) : RecyclerView.ViewHolder(view){

    }

    //  Set different layouts
    private fun setLayoutPrivatePosts(holder: MyPostsAdapter.ViewHolderPrivate, position: Int) {
        if ((listOfPosts[position].post as LocalPost).listOfPostTags.isNullOrEmpty()) {
            holder.tagsRecyclerView.visibility = View.GONE
        }

        else{
            holder.tagsRecyclerView.visibility = View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            holder.tagsRecyclerView.layoutManager = linearLayoutManager
            holder.tagsRecyclerView.adapter =
                TagsAdapter((listOfPosts[position].post as LocalPost).listOfPostTags.toMutableList(), this)
        }

        holder.apply {
            val postStatus = (listOfPosts[position].post as LocalPost).status
            if (postStatus.isNotEmpty()){
                status.text = postStatus
                status.visibility = View.VISIBLE
            }
            else{
                status.visibility = View.GONE
            }
            publishedTime.text = (listOfPosts[position].post as LocalPost).published_time
            body.text = (listOfPosts[position].post as LocalPost).body
        }
    }

    private fun setLayoutPublishedPosts(holder: MyPostsAdapter.ViewHolderPublished, position: Int) {

    }

    private fun setLayoutModerationPosts(holder: MyPostsAdapter.ViewHolderModeration, position: Int) {
        val remotePosts = listOfPosts[position].post as RemotePost
        if (remotePosts.listOfTags.isNullOrEmpty()) {
            holder.tagsRecyclerView.visibility = View.GONE
        }

        else{
            holder.tagsRecyclerView.visibility = View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            holder.tagsRecyclerView.layoutManager = linearLayoutManager
            val tags = remotePosts.listOfTags
            val listOfTags = tags
            holder.tagsRecyclerView.adapter = listOfTags?.let { TagsAdapter(it.toList(), this) }
        }

        holder.apply {
            val postStatus = remotePosts.status
            if (!postStatus.isNullOrEmpty()){
                status.text = postStatus
                status.visibility = View.VISIBLE
            }
            else{
                status.visibility = View.GONE
            }
            publishedTime.text = remotePosts.published_time
            body.text = remotePosts.body


        }
    }


    //  Time that story takes to read
    private fun takesTimeToRead(text: String, context: Context): String{
        fun countTimeToRead(text: String): Int{
            val countOfWords =  text.trim().split("\\s+".toRegex()).size
            return countOfWords / 150
        }
        return when (val time = countTimeToRead(text)){
            0 -> "Займет менее минуты"
            else -> {
                context.resources.getQuantityString(R.plurals.time, time, time)
            }
        }
    }

    override fun onTagClicked(postTag: Tag) {

    }

}