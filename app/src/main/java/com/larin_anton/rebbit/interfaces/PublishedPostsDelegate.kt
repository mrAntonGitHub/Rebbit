package com.larin_anton.rebbit.interfaces

import com.larin_anton.rebbit.data.model.RemotePost

interface PublishedPostsDelegate {
    suspend fun onComplainAboutClicked(remotePost: RemotePost)
    fun onLikeClicked(remotePost: RemotePost, isLikeSet: Boolean)
    suspend fun onCommentClicked(remotePost: RemotePost)
    suspend fun onAuthorClicked(authorId: String)
    suspend fun onAddToBookmarksClicked(remotePost: RemotePost, isAddSet: Boolean)
}