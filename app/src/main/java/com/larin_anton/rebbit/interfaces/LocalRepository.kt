package com.larin_anton.rebbit.interfaces

import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.LocalPost

interface LocalRepository {
    // Post
    suspend fun getPrivatePost(postId: Long): LocalPost
    suspend fun getAllPrivatePosts(): List<LocalPost>
    suspend fun addPrivatePost(post: LocalPost): Long
    suspend fun addPrivatePost(posts: List<LocalPost>) : List<Long>
    suspend fun editPrivatePost(post: LocalPost)
    suspend fun removePrivatePost(postId: Long)

    // Post Tags
    suspend fun getPrivatePostTags(postId: Long): List<Tag>
    suspend fun addPrivatePostTags(postId: Long, listOfTags: List<Tag>)
    suspend fun removePrivatePostTags(postId: Long)

    // Tags
    suspend fun getAllTags(): List<Tag>
    suspend fun addTags(setOfTags: Set<Tag>)
    suspend fun removeTag(tag: Tag)
    suspend fun removeAllTags()

    // Remove all
    fun removeAllTables()
}