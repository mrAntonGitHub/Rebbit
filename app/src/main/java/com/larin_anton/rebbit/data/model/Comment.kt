package com.larin_anton.rebbit.data.model


data class Comment (
    var commentId: String,
    val userName: String,
    val userId: String,
    val userIcon: String,
    val commentText: String,
    val time: String
){
    constructor() : this("", "", "", "", "", "")
}