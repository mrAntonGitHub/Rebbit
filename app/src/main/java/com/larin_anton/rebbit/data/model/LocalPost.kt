package com.larin_anton.rebbit.data.model

import androidx.room.*

@Entity(tableName = "local_post")
data class LocalPost constructor(

    @ColumnInfo(name = "status")
    var status: String,

    @ColumnInfo(name = "published_time")
    val published_time: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "body")
    val body: String,
) {

    constructor() : this("","", "", "")

    @PrimaryKey(autoGenerate = true)
    var postId: Long = 0

    @Ignore
    var listOfPostTags: MutableList<Tag> = mutableListOf()
}