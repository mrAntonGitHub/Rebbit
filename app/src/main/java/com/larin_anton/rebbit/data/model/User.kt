package com.larin_anton.rebbit.data.model

data class User (
    var id: String,
    var name: String,
    var status: UserStatus? = UserStatus.Plain,
    var phoneNumber: String?,
    var email: String?,
    var image: String,
    var rating: String = "0",
    var isHidden: String = "false",
    var listOfPrivateTags : HashMap<String, Tag>?,
    var listOfLikesPostId : HashMap<String, String>?,
    var listOfRemotePosts : HashMap<String, RemotePost>?,
    var listOfPersonalPosts : HashMap<String, LocalPost>?,
    var listOfBookmarks : HashMap<String, RemotePost>?,
    var listOfNotifications : HashMap<String, Any>?,
){
    constructor() : this("", "", null, "", "", "", "", "", null, null, null, null, null, null)
}