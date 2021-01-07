package com.larin_anton.rebbit.repository

import com.larin_anton.rebbit.data.local.LocalDatabase
import com.larin_anton.rebbit.data.local.dao.LocalDao
import com.larin_anton.rebbit.data.model.LocalPost
import com.larin_anton.rebbit.data.model.PostTag
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.interfaces.LocalRepository
import javax.inject.Inject
import javax.inject.Singleton

const val STATUS_UNPUBLISHED = "Не опубликован"
@Singleton
class LocalRepository @Inject constructor(private val localDatabase: LocalDatabase) : LocalRepository {
    private var localDao: LocalDao = localDatabase.localPostDao()

    // Post
    override suspend fun getPrivatePost(postId: Long): LocalPost {
        val post = localDao.getPost(postId)
        val listOfTags : List<Tag> = getPrivatePostTags(postId)
        if (listOfTags.isNotEmpty()){
            post.listOfPostTags = listOfTags.toMutableList()
        }
        return post
    }
    override suspend fun getAllPrivatePosts(): List<LocalPost> {
        val posts = localDao.getAllPosts()
        posts.forEach {
            val listOfTags : List<Tag> = getPrivatePostTags(it.postId)
            if (listOfTags.isNotEmpty()){
                it.listOfPostTags = listOfTags.toMutableList()
            }
        }
        return posts
    }
    override suspend fun addPrivatePost(post: LocalPost): Long {
        post.status = STATUS_UNPUBLISHED
        val id = localDao.addPost(post)
        val listOfTag = mutableListOf<PostTag>()
        post.listOfPostTags.forEach {
            listOfTag.add(PostTag(it, id.toString()))
        }
        localDao.addPostTags(listOfTag)
        return id
    }
    override suspend fun addPrivatePost(posts: List<LocalPost>) : List<Long>{
        val listOfIds = mutableListOf<Long>()
        posts.forEach {
            it.status = STATUS_UNPUBLISHED
            val id = localDao.addPost(it)
            listOfIds.add(id)
            val listOfPostTags = mutableListOf<PostTag>()
            it.listOfPostTags.forEach{tag ->
                listOfPostTags.add(PostTag(tag, id.toString()))
            }
            localDao.addPostTags(listOfPostTags)
        }
        return listOfIds
    }
    override suspend fun editPrivatePost(post: LocalPost) {
        post.status = STATUS_UNPUBLISHED
        localDao.addPost(post)
        post.postId.let { localDao.removeAllPostTags(it) }
        val listOfTag = mutableListOf<PostTag>()
        post.listOfPostTags.forEach {
            listOfTag.add(PostTag(it, post.postId.toString()))
        }
        localDao.addPostTags(listOfTag)
    }
    override suspend fun removePrivatePost(postId: Long) {
        localDao.removeAllPostTags(postId)
        localDao.removePost(postId)
    }

    // Post Tags
    override suspend fun getPrivatePostTags(postId: Long): List<Tag> {
        val listOfPostTags = localDao.getPostTags(postId)
        return listOfPostTags.map {
            Tag(it.tag.name)
        }
    }
    override suspend fun addPrivatePostTags(postId: Long, listOfTags: List<Tag>) {
        val listOfPostTag = listOfTags.map {
            PostTag(it, postId.toString())
        }
        localDao.addPostTags(listOfPostTag)
    }
    override suspend fun removePrivatePostTags(postId: Long) {
        localDao.removeAllPostTags(postId)
    }

    // Tags
    override suspend fun getAllTags(): List<Tag> {
        return localDao.getAllTags()
    }
    override suspend fun addTags(setOfTags: Set<Tag>) {
        localDao.addTags(setOfTags.toList())
    }
    override suspend fun removeTag(tag: Tag) {
        localDao.removeTag(tag)
    }
    override suspend fun removeAllTags() {
        localDao.removeAllTagsTable()
    }

    // Remove all
    override fun removeAllTables() {
        localDatabase.clearAllTables()
    }
}