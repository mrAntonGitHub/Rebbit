package com.larin_anton.rebbit.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["tag_name", "parentId"], tableName = "post_tags",
    foreignKeys = [ForeignKey(
        entity = LocalPost::class,
        parentColumns = arrayOf("postId"),
        childColumns = arrayOf("parentId")
    )]
)
data class PostTag(
    @Embedded(prefix = "tag_")
    val tag: Tag,

    var parentId: String = ""
) {
    constructor() : this( Tag(""), "")
}