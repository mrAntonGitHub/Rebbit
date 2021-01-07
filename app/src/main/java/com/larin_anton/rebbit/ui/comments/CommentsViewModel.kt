package com.larin_anton.rebbit.ui.comments

import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.Comment
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.repository.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var utils: com.larin_anton.rebbit.utils.Utils

    init {
        Application.application.appComponent.inject(this)
    }

    suspend fun getPost(postId: String) : RemotePost?{
        return repository.getPost(postId)
    }

    suspend fun getCurrentUser() : User?{
        return repository.getCurrentUser()
    }

    suspend fun sendComment(postId: String, commentText : String){
        val user = repository.getCurrentUser()
        val currentTime = utils.getUnixTime()

        val userName = user?.name
        val userId = user?.id
        val userImage = user?.image

        if (userName != null && userId != null && userImage != null){
            val comment = Comment("", userName, userId, userImage, commentText, currentTime)
            repository.commentPost(postId, comment)
        }
    }

    suspend fun removeComment(postId: String, comment: Comment){
        repository.removeOwnComment(postId, comment)
    }

}