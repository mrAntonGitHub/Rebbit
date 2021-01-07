package com.larin_anton.rebbit.ui.comments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.CommentsAdapter
import com.larin_anton.rebbit.adapters.TagsAdapter
import com.larin_anton.rebbit.data.model.Comment
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.ui.home.allStories.POST_ID
import com.larin_anton.rebbit.utils.DateFormat
import com.larin_anton.rebbit.utils.Utils.Companion.millisecondsToStringPattern
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val HAVE_NOT_ENTERED = "Вы ничего не ввели"

class CommentsActivity : AppCompatActivity(), TagsAdapter.OnTagAdapterAction,
    CommentsAdapter.OnCommentAction {

    private val listOfComments : MutableList<Comment> = mutableListOf()
    lateinit var adapter: CommentsAdapter

    @Inject
    lateinit var commentsViewModel: CommentsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        Application.application.appComponent.inject(this)

        // Set send comment button color
        val sendColorAvailable = R.color.main_primaryColor
        val sendColorNonAvailable = R.color.gray

        val postId = intent.getStringExtra(POST_ID)
        postId?.let { loadPost(postId) }

        // Send comment
        comments_sendCommentButton.setOnClickListener {
            val commentText = comments_comment.text.toString()
            if (commentText.trim().isEmpty()) {
                Toast.makeText(this, HAVE_NOT_ENTERED, Toast.LENGTH_SHORT).show()
            } else {
                postId?.let { postId -> sendComment(postId, commentText) }
            }
        }

        // Listener to change send button color
        comments_comment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (comments_comment.text.trim().isNotEmpty()) {
                    comments_sendCommentButton.imageTintList = ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            sendColorAvailable,
                            null
                        )
                    )
                } else {
                    comments_sendCommentButton.imageTintList = ColorStateList.valueOf(
                        ResourcesCompat.getColor(
                            resources,
                            sendColorNonAvailable,
                            null
                        )
                    )
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // Refresh comments
        comments_refresh.setOnRefreshListener {
            if (postId != null){
                CoroutineScope(IO).launch {
                    refreshComments(postId)
                }
            }
        }

        smoothScrollToRecyclerView()
    }

    private fun smoothScrollToRecyclerView() {
        CoroutineScope(IO).launch {
            delay(500)
            withContext(Main){
                comments_nestedScrollView.smoothScrollTo(0, comments_commentsRecyclerView.top, 2_000)
            }
        }
    }

    private fun loadPost(postId: String) {
        CoroutineScope(IO).launch {
            val post = commentsViewModel.getPost(postId)
            if (post != null) {
                withContext(Main) {
                    setPost(post)
                }
            }else{
                Log.e("CommentsActivity", "onCreate [Post is null]")
            }
        }
    }

    private fun sendComment(postId: String, commentText: String) {
        comments_comment.text.clear()
        hideKeyboardFrom(this, comments_comment)
        comments_comment.clearFocus()
        CoroutineScope(IO).launch {
            commentsViewModel.sendComment(postId, commentText)
            refreshComments(postId)
        }
    }

    private fun setPost(remotePost: RemotePost){
        CoroutineScope(IO).launch {
            val user = commentsViewModel.getCurrentUser()
            if(remotePost.listOfLikedUsersId != null && remotePost.listOfLikedUsersId.contains(user?.id)){
                withContext(Main){
                    comments_like.isSelected = true
                }
            }
        }
        comments_progressBar.visibility = View.GONE
        comments_publishedTimeTextView.text = remotePost.published_time.millisecondsToStringPattern(
            DateFormat.DayMonthTimeWithSeconds
        )
        comments_bodyTextView.text = remotePost.body
        comments_author.text = remotePost.author
        val tagsAdapter = remotePost.listOfTags?.let { TagsAdapter(it, this) }
        comments_tagsRecyclerView.adapter = tagsAdapter
        comments_like.text = (remotePost.listOfLikedUsersId?.size ?: 0).toString()

        val comments = remotePost.listOfComments?.values?.toMutableList()
        comments.let { list ->
            list?.sortBy { it.time }
            list?.reverse()
        }

        if (comments != null){
            initAdapter(comments)
        }

    }

    private fun initAdapter(comments: List<Comment>){
        listOfComments.clear()
        listOfComments.addAll(comments)
        adapter = CommentsAdapter(listOfComments, this)
        comments_commentsRecyclerView.adapter = adapter
    }

    private suspend fun refreshComments(postId: String){
        val post = commentsViewModel.getPost(postId)
        if (post != null) {
            withContext(Main){
                comments_refresh.isRefreshing = false

                val comments = post.listOfComments?.values?.toMutableList()
                comments.let { list ->
                    list?.sortBy { it.time }
                    list?.reverse()
                }

                updateAdapter(comments?.toList() ?: emptyList())
            }
        }
        comments_nestedScrollView.smoothScrollTo(0, comments_commentsRecyclerView.top, 4_000)
    }

    private fun updateAdapter(comments: List<Comment>){
        listOfComments.clear()
        listOfComments.addAll(comments)
        adapter.notifyDataSetChanged()
    }

    override fun onTagClicked(postTag: Tag) {
        Log.e("CommentsActivity", "onTagClicked [Clicked!]")
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun complainOnComment(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun removeComment(comment: Comment) {
        val postId = intent.getStringExtra(POST_ID)
        CoroutineScope(IO).launch {
            if (postId != null) {
                commentsViewModel.removeComment(postId.toString(), comment)
                refreshComments(postId)
            }
        }
    }

    override fun editComment(comment: Comment) {
        TODO("Not yet implemented")
    }

    override fun onReply(comment: Comment) {
        TODO("Not yet implemented")
    }
}