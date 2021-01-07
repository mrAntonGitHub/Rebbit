package com.larin_anton.rebbit.data.model.notifications

import com.larin_anton.rebbit.data.model.RemotePost

data class NotificationLike (
    val postId: String,
    val post: RemotePost,
    val likeId: String,
    val likedById: String,
    val likedByName: String,
    val likedByIcon: String,
    val time: String
){
    constructor() : this("", RemotePost(), "", "", "", "", "")
}