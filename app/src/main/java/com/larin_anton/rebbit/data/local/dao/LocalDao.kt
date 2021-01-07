package com.larin_anton.rebbit.data.local.dao

import androidx.room.*
import com.larin_anton.rebbit.data.model.PostTag
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.LocalPost

@Dao
interface LocalDao {

    ////////////////////////  Posts  ////////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPost(post: LocalPost) : Long

    @Query("SELECT * FROM local_post WHERE postId IS :postId")
    suspend fun getPost(postId: Long): LocalPost

    @Query("SELECT * FROM local_post")
    suspend fun getAllPosts(): List<LocalPost>

    @Query("DELETE FROM local_post WHERE postId = :postId")
    suspend fun removePost(postId: Long)

    @Query("DELETE FROM local_post")
    suspend fun removePostsTable()


    ////////////////////////  Post Tags  ////////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPostTags(listOfPostTag: List<PostTag>)

    @Query("SELECT * FROM post_tags WHERE parentId IS :postId")
    suspend fun getPostTags(postId: Long): List<PostTag>

    @Query("DELETE FROM post_tags WHERE parentId = :postId")
    suspend fun removeAllPostTags(postId: Long)

    @Query("DELETE FROM post_tags")
    suspend fun removePostTagsTable()


    ////////////////////////  Tags  ////////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTags(listOfTags: List<Tag>):  List<Long>

    @Query("SELECT * FROM all_tags")
    suspend fun getAllTags() : List<Tag>

    @Delete
    suspend fun removeTag(tag: Tag)

    @Query("DELETE FROM all_tags")
    suspend fun removeAllTagsTable()
}