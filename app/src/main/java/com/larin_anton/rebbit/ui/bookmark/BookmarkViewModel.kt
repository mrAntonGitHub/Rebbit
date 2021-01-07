package com.larin_anton.rebbit.ui.bookmark

import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.repository.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var repository: Repository

    init {
        Application.application.appComponent.inject(this)
    }

    suspend fun getBookmarksPosts() : List<RemotePost>?{
        return repository.getBookmarksPosts()
    }

    suspend fun getBookedPostsIds() : List<String>?{
        return repository.getBookmarksPosts()?.map { it.postId }
    }

    suspend fun getCurrentUser() : User?{
        return repository.getCurrentUser()
    }

}