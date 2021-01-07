package com.larin_anton.rebbit.interfaces

import com.larin_anton.rebbit.data.model.*

interface RemoteRepository {

    // General methods
    suspend fun getListOfTags(): Set<Tag>?
    suspend fun getAllUsers(): List<User>?
    fun addNewUser(user: User)
    suspend fun increaseUser(userId: String): Boolean
    suspend fun decreaseUser(userId: String): Boolean

    // Current user methods
    suspend fun getCurrentUser(): User?
    suspend fun setCurrentUser(): Boolean
    suspend fun removeCurrentUserAccount(): Boolean
    suspend fun getListOfUserPublishedPosts(): List<RemotePost>?
    suspend fun getListOfUserOnModerationPosts(): List<RemotePost>?
    suspend fun getListOfUserPrivatePosts() : List<LocalPost>?
    fun signOutCurrentUser()

    // Post
     // General
    suspend fun getPost(postId: String) : RemotePost?
    suspend fun setLikePost(postId: String): Boolean
    suspend fun removeLikePost(postId: String): Boolean
    suspend fun commentPost(postId: String, comment: Comment): Boolean

    // On moderation posts
    suspend fun addOnModerationPost(post: RemotePost)
    suspend fun moveOnModerationPostToPublished(postId: String)
    suspend fun rejectOnModerationPost(postId: String)
    suspend fun editOnModerationPost(post: RemotePost)
    suspend fun getOnModerationPost(postId: String) : RemotePost?
    // Private posts
    suspend fun addPrivatePost(post: LocalPost)
    suspend fun addPrivatePost(listOfPosts: List<LocalPost>)
    suspend fun removePrivatePost(postId: String)
    suspend fun removeAllPrivatePosts()
     // Published posts
    suspend fun removePublishedPost(postId: String)
     // Complain
    suspend fun complainAboutPost(postId: String)

    // Tags
    suspend fun addTag(tag: Tag)

}