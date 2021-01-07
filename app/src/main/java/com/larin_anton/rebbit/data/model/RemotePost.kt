package com.larin_anton.rebbit.data.model

data class RemotePost constructor(

    var status: String?,

    val published_time: String,

    val authorId: String,

    val author: String,

    val listOfLikedUsersId: HashMap<String, String>?,

    val listOfComments: HashMap<String, Comment>?,

    val title: String?,

    val body: String,

    var listOfTags: List<Tag>? = listOf()
) {

    constructor() : this(null, "", "", "", null,  null, null, "", null)

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass){
            return false
        }
        other as RemotePost
        if (status != other.status) return false
        if (published_time != other.published_time) return false
        if (authorId != other.authorId) return false
        if (author != other.author) return false
        if (listOfLikedUsersId != other.listOfLikedUsersId) return false
        if (listOfComments != other.listOfComments) return false
        if (title != other.title) return false
        if (body != other.body) return false
        if (listOfTags != other.listOfTags) return false

        return true
    }

    var postId: String = ""
}