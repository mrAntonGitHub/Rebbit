package com.larin_anton.rebbit.interfaces

import com.larin_anton.rebbit.data.model.RemotePost

interface BookedPostsDelegate {
    suspend fun onComplainAboutClicked(remotePost: RemotePost)
    suspend fun onLikeClicked(remotePost: RemotePost, isLikeSet: Boolean)
    suspend fun onCommentClicked(remotePost: RemotePost)
    suspend fun onAuthorClicked(authorId: String)
    suspend fun onAddToBookmarksClicked(postId: String)
}