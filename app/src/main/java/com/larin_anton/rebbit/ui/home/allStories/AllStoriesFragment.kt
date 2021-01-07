package com.larin_anton.rebbit.ui.home.allStories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.PostAdapter
import com.larin_anton.rebbit.data.model.*
import com.larin_anton.rebbit.interfaces.PublishedPostsDelegate
import com.larin_anton.rebbit.ui.comments.CommentsActivity
import com.larin_anton.rebbit.ui.dialogs.ComplainDialogFragment
import com.larin_anton.rebbit.utils.LiveDataWrapper
import com.larin_anton.rebbit.utils.Utils.Companion.showDialog
import kotlinx.android.synthetic.main.fragment_all_stories.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject
import kotlin.collections.HashMap

const val DIALOG_NAME = "Пожаловаться"
const val POST_ID = "POST_ID"

class AllStoriesFragment : Fragment(), ComplainDialogFragment.ComplainAboutAction,
    PublishedPostsDelegate {

    @Inject
    lateinit var allStoriesViewModel: AllStoriesViewModel

    private val actualList = mutableListOf<RemotePost>()
    private val currentList = mutableListOf<RemotePost>()

    // Set or remove post's likes to send to firebase later
    private val likedPosts = HashMap<String, Boolean>()

    val adapter = PostAdapter()

    // Observer for published posts
    lateinit var publishedPostsObserver : Observer<LiveDataWrapper<List<RemotePost>>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Application.application.appComponent.inject(this)
        return inflater.inflate(R.layout.fragment_all_stories, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        allStories_recyclerView.adapter = adapter
        adapter.setDelegate(this)

        // Observe remote posts changes
        publishedPostsObserver = Observer{
            if (it.status == LiveDataStatus.SUCCESS){
                if (it.data != null){
                    actualList.clear()
                    actualList.addAll(it.data)
                    if (currentList.isEmpty()){
                        CoroutineScope(IO).launch {
                            val user = allStoriesViewModel.user.await()
                            withContext(Main){
                                adapter.apply {
                                    setUser(user)
                                    setActualData(it.data)
                                }
                            }
                        }
                    }else{
                        if (currentList.size != it.data.size){
                            newPostsFound()
                        }
                    }
                    showOrHideElements(adapter.itemCount > 0)
                }
            }
        }

        allStories_newStoriesFound.setOnClickListener {
            allStories_recyclerView.smoothScrollToPosition(adapter.itemCount)
        }

        allStories_updateData.setOnRefreshListener {
            CoroutineScope(IO).launch {
                likedPosts.forEach {
                    it.key
                    allStoriesViewModel.likePost(it.key, it.value)
                }
                withContext(Main){
                    likedPosts.clear()
                    setActualData(actualList)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        likedPosts.forEach {
            allStoriesViewModel.likePost(it.key, it.value)
        }
        likedPosts.clear()
    }

    private fun setActualData(listOfRemotePosts: List<RemotePost>){
        /* Set actual data */
        currentList.clear()
        currentList.addAll(listOfRemotePosts)

        adapter.submitList(listOfRemotePosts)
        allStories_newStoriesFound.visibility = View.GONE
        allStories_recyclerView.smoothScrollToPosition(adapter.itemCount)
        allStories_updateData.isRefreshing = false
    }

    private fun newPostsFound(){
        allStories_newStoriesFound.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        allStoriesViewModel.listOfRemotePost.observe(this, publishedPostsObserver)
    }

    override fun onStop() {
        super.onStop()
        allStoriesViewModel.listOfRemotePost.removeObserver(publishedPostsObserver)
    }

    fun showOrHideElements(isRecyclerViewEmpty: Boolean) {
        if (isRecyclerViewEmpty) {
            allStories_noStories.visibility = View.VISIBLE
        } else {
            allStories_noStories.visibility = View.GONE
        }
    }

    override suspend fun onComplainAboutClicked(remotePost: RemotePost) {
        if (isAdded){
            showDialog(remotePost, requireActivity(), this)
        }else{
            Log.e("AllStoriesFragment", "showDialog [Fragment is not attached to Activity]")
        }
    }

    override fun onLikeClicked(remotePost: RemotePost, isLikeSet: Boolean) {
        val index = currentList.indexOfFirst { it.postId == remotePost.postId }

        currentList[index] = remotePost
        likedPosts[remotePost.postId] = isLikeSet
    }

    override suspend fun onCommentClicked(remotePost: RemotePost) {
        // todo
        val intentToOpenCommentsPlace = Intent(requireContext(), CommentsActivity::class.java)
        intentToOpenCommentsPlace.putExtra(POST_ID, remotePost.postId)
        startActivity(intentToOpenCommentsPlace)
    }

    override suspend fun onAuthorClicked(authorId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun onAddToBookmarksClicked(remotePost: RemotePost, isAddSet: Boolean) {
        allStoriesViewModel.addToBookmarks(remotePost.postId, isAddSet)
    }

    override suspend fun onSendComplainClicked(remotePost: RemotePost, complainType: String, complainDescription: String) {
        /* User wants to complain about the post */
        val currentUserId = allStoriesViewModel.user.await()?.id
        val complainPost = ComplainPost(currentUserId.toString(), remotePost.postId, complainType, complainDescription)
        allStoriesViewModel.complainAboutPost(complainPost)
    }

}