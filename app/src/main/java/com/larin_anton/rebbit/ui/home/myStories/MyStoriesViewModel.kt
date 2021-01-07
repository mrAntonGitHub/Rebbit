package com.larin_anton.rebbit.ui.home.myStories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.adapters.MyPostsAdapter
import com.larin_anton.rebbit.data.model.LocalPost
import com.larin_anton.rebbit.data.model.PostType
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.utils.LiveDataWrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.text.SimpleDateFormat
import java.util.*

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyStoriesViewModel @Inject constructor(private val repository: Repository) : ViewModel(), MyPostsAdapter.OnMyPostsActions {

    ////////////////////////  Live data to update View  ////////////////////////
    private val _listOfUserPrivatePosts : MutableLiveData<LiveDataWrapper<List<LocalPost>>> = MutableLiveData()
    val listOfUserPrivatePosts  : LiveData<LiveDataWrapper<List<LocalPost>>> = _listOfUserPrivatePosts

    private val _listOfUserPublishedPosts : MutableLiveData<LiveDataWrapper<List<RemotePost>>> = MutableLiveData()
    val listOfUserPublishedPosts : LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfUserPublishedPosts

    private val _listOfUserOnModerationPosts : MutableLiveData<LiveDataWrapper<List<RemotePost>>> = MutableLiveData()
    val listOfUserOnModerationPosts : LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfUserOnModerationPosts

    private val _currentRecyclerViewState : MutableLiveData<LiveDataWrapper<PostType>> = MutableLiveData()
    val currentRecyclerViewState : LiveData<LiveDataWrapper<PostType>> = _currentRecyclerViewState

    init {
        Application.application.appComponent.inject(this)
        CoroutineScope(IO).launch {
            updateData()
        }
        _currentRecyclerViewState.value = LiveDataWrapper.success(PostType.ALL)
    }

    suspend fun updateData(){
        setListOfUserPrivatePosts()
        setListOfUserPublishedPosts()
        setListOfUserOnModerationPosts()
    }

    private suspend fun setListOfUserPrivatePosts(){
        val privatePosts = repository.getAllPrivatePosts()
        withContext(Main){
            _listOfUserPrivatePosts.value = LiveDataWrapper.success(privatePosts)
            Log.i("MyStoriesViewModel", "setListOfUserPrivatePosts [Got private posts from firebase size: ${privatePosts.size}]")
        }
    }
    private suspend fun setListOfUserPublishedPosts(){
        val publishedPosts = repository.getCurrentUserOnModerationPosts()
        withContext(Main){
            _listOfUserOnModerationPosts.value = LiveDataWrapper.success(publishedPosts)
            Log.i("MyStoriesViewModel", "setListOfUserPublishedPosts [Got remote posts from firebase size: ${publishedPosts?.size}]")
        }
    }
    private suspend fun setListOfUserOnModerationPosts(){
        val publishedPosts = repository.getCurrentUserOnModerationPosts()
        withContext(Main){
            _listOfUserOnModerationPosts.value = LiveDataWrapper.success(publishedPosts)
            Log.i("MyStoriesViewModel", "setListOfUserPublishedPosts [Got remote posts from firebase size: ${publishedPosts?.size}]")
        }
    }


    fun setCurrentRecyclerViewType(recyclerViewType: PostType){
        when(recyclerViewType){
            PostType.PRIVATE -> {
                Log.d("MyStoriesViewModel", "setCurrentRecyclerViewType [Set private posts]")
                _currentRecyclerViewState.value = LiveDataWrapper.success(PostType.PRIVATE)
            }
            PostType.MODERATION -> {
                Log.d("MyStoriesViewModel", "setCurrentRecyclerViewType [Set moderated posts]")
                _currentRecyclerViewState.value = LiveDataWrapper.success(PostType.MODERATION)
            }
            PostType.PUBLISHED -> {
                Log.d("MyStoriesViewModel", "setCurrentRecyclerViewType [Set published posts]")
                _currentRecyclerViewState.value = LiveDataWrapper.success(PostType.PUBLISHED)
            }
            PostType.ALL -> {
                Log.d("MyStoriesViewModel", "setCurrentRecyclerViewType [Set all posts]")
                _currentRecyclerViewState.value = LiveDataWrapper.success(PostType.ALL)
            }
        }
    }


    override fun onDeleteClicked(localPost: LocalPost) {
        CoroutineScope(IO).launch {
            val postId = localPost.postId
            if (postId != null){
                repository.removePrivatePost(postId)
                withContext(Main){
                    updateData()
                }
            }
        }
    }

    override fun onPostClicked(postId: Long) {

    }

    override fun onPublishClicked(localPost: LocalPost) {
        val title: String = localPost.title.toString()
        val body: String = localPost.body
        val currentTime = SimpleDateFormat("dd.MM. HH:mm", Locale.getDefault()).format(Date())
        val remotePost = RemotePost("", currentTime, "","", null, null, title, body)
        CoroutineScope(IO).launch {
            repository.addOnModerationPost(remotePost)
            val postId = localPost.postId
            if (postId != null){
                repository.removePrivatePost(postId)
                withContext(Main){
                    updateData()
                }
            }
        }
    }

    override fun onEditOnModerationPostClicked(remotePost: RemotePost) {

    }

    override fun onCancelModerating(remotePost: RemotePost) {
        TODO("Not yet implemented")
    }

    override fun onDeletePublishedPostClicked(remotePost: RemotePost) {
        TODO("Not yet implemented")
    }
}