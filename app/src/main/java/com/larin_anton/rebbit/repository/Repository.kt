package com.larin_anton.rebbit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.larin_anton.rebbit.data.model.*
import com.larin_anton.rebbit.data.model.notifications.Notification
import com.larin_anton.rebbit.utils.LiveDataWrapper
import com.larin_anton.rebbit.interfaces.Repository
import javax.inject.Inject
import javax.inject.Singleton

/**
* Main repository, which connects with local and remote repositories
*/

@Singleton
class Repository @Inject constructor(private val localRepository: LocalRepository, private val remoteRepository: RemoteRepository) : Repository {

    /**
     * LiveData which observe changes in published / onModeration / complainedAbout posts
     */
    // Published Posts
    private val _listOfCheckedPosts : MutableLiveData<LiveDataWrapper<List<RemotePost>>> = remoteRepository.listOfPublishedPosts as MutableLiveData<LiveDataWrapper<List<RemotePost>>>
    val listOfCheckedPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfCheckedPosts

    // Unchecked Posts
    private val _listOfOnModerationPosts : MutableLiveData<LiveDataWrapper<List<RemotePost>>> = remoteRepository.listOfOnModerationPosts as MutableLiveData<LiveDataWrapper<List<RemotePost>>>
    val listOfOnModerationPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfOnModerationPosts

    // Posts that were complained about
    private val _listOfComplainedPosts : MutableLiveData<LiveDataWrapper<List<RemotePost>>> = remoteRepository.listOfComplainPosts as MutableLiveData<LiveDataWrapper<List<RemotePost>>>
    val listOfComplainedPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfComplainedPosts

    // User's Notifications
    val notifications : LiveData<LiveDataWrapper<List<Notification>>> = remoteRepository.notifications


    /**
     * General
     */
    fun addNewUser(user: User){
        remoteRepository.addNewUser(user)
    }
    suspend fun increaseUser(userId: String){
        remoteRepository.increaseUser(userId)
    }
    suspend fun decreaseUser(userId: String){
        remoteRepository.decreaseUser(userId)
    }


    /**
     * Current user
     */
    suspend fun getCurrentUser(): User?{
        return remoteRepository.getCurrentUser()
    }
    suspend fun setCurrentUser(){
        remoteRepository.setCurrentUser()
    }
    suspend fun removeCurrentUser(){
        remoteRepository.removeCurrentUserAccount()
        localRepository.removeAllTables()
    }

    /**
     * Post
     */
    // General
    suspend fun likePost(postId: String, isLikeSet: Boolean){
        Log.e("12222212", "$isLikeSet")
        if (isLikeSet){
            remoteRepository.setLikePost(postId)
        }else{
            remoteRepository.removeLikePost(postId)
        }
    }
    suspend fun getPost(postId: String) : RemotePost?{
        return remoteRepository.getPost(postId)
    }

    // On moderation posts
    suspend fun addOnModerationPost(post: RemotePost){
        remoteRepository.addOnModerationPost(post)
    }
    suspend fun getCurrentUserOnModerationPost(postId: String) : RemotePost?{
        return remoteRepository.getOnModerationPost(postId)
    }
    suspend fun getCurrentUserOnModerationPosts() : List<RemotePost>?{
        return remoteRepository.getListOfUserOnModerationPosts()
    }
    suspend fun editOnModerationPost(post: RemotePost){
        remoteRepository.editOnModerationPost(post)
    }

    // Private posts
    suspend fun removeAllRemotePrivatePosts() {
        remoteRepository.removeAllPrivatePosts()
    }
    suspend fun getPrivatePost(postId: Long) : LocalPost{
        return localRepository.getPrivatePost(postId)
    }
    suspend fun getAllPrivatePosts() : List<LocalPost>{
        return localRepository.getAllPrivatePosts()
    }
    suspend fun removePrivatePost(postId: Long){
        localRepository.removePrivatePost(postId)
    }
    suspend fun addPrivatePost(post: LocalPost){
        localRepository.addPrivatePost(post)
    }
    suspend fun editPrivatePost(post: LocalPost){
        localRepository.editPrivatePost(post)
    }

    // Published posts
    suspend fun getListOfUserPublishedPosts() : List<RemotePost>?{
        return remoteRepository.getListOfUserPublishedPosts()
    }
    suspend fun commentPost(postId: String, comment: Comment){
        remoteRepository.commentPost(postId, comment)
    }
    suspend fun addPostToBookmarks(postId: String, isAddSet: Boolean) : Boolean{
        return remoteRepository.addPostToBookmarks(postId, isAddSet)
    }
    suspend fun getBookmarksPosts() : List<RemotePost>?{
        return remoteRepository.getBookmarksPosts()
    }
    suspend fun removeOwnComment(postId: String, comment: Comment){
        remoteRepository.removeOwnComment(postId, comment)
    }

    // Complain
    suspend fun sendComplain(complainPost: ComplainPost){
        Log.e("Repository", "sendComplain [${complainPost}]")
    }

    // Notifications
    suspend fun removeLikeNotification(likeNotificationId: String) : Boolean{
        return remoteRepository.removeLikeNotification(likeNotificationId)
    }


    /**
     * Tags
     */
    suspend fun getTags() : Set<Tag>?{
        return remoteRepository.getListOfTags()
    }
    suspend fun addRemoteTag(tag: Tag) {
        remoteRepository.addTag(tag)
    }


    /**
     * Update data
     */
    suspend fun replaceCurrentUserLocalPrivatePosts(){
        // Update local repository data from firebase
        localRepository.removeAllTables()
        val posts = remoteRepository.getListOfUserPrivatePosts()
        if (posts != null) {
            localRepository.addPrivatePost(posts)
        }
    }
    suspend fun replaceCurrentUserRemotePrivatePosts(){
        remoteRepository.removeAllPrivatePosts()
        val posts = localRepository.getAllPrivatePosts()
        remoteRepository.addPrivatePost(posts)
    }

    /**
     * Sign out
     */
    fun signOut(){
        remoteRepository.signOutCurrentUser()
        localRepository.removeAllTables()
    }

}


