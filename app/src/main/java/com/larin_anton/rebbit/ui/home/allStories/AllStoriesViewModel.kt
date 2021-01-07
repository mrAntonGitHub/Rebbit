package com.larin_anton.rebbit.ui.home.allStories

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.ComplainPost
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.utils.LiveDataWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AllStoriesViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    init {
        Application.application.appComponent.inject(this)
    }

    val listOfRemotePost: LiveData<LiveDataWrapper<List<RemotePost>>> = repository.listOfCheckedPosts

    val user: CompletableDeferred<User?> by lazy{
        CompletableDeferred<User?>().apply {
            CoroutineScope(IO).launch {
                val currentUser = async { repository.getCurrentUser() }
                complete(currentUser.await())
            }
        }
    }

    fun likePost(remotePostId: String, isLikeSet: Boolean){
        CoroutineScope(IO).launch {
            repository.likePost(remotePostId, isLikeSet)
        }
    }

    suspend fun complainAboutPost(complainPost: ComplainPost){
        repository.sendComplain(complainPost)
    }

    suspend fun addToBookmarks(postId: String, isAddSet: Boolean){
        repository.addPostToBookmarks(postId, isAddSet)
    }

    suspend fun getBookedPostsIds() : List<String>?{
        return repository.getBookmarksPosts()?.map { it.postId }
    }
}