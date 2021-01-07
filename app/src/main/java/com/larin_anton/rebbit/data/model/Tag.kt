package com.larin_anton.rebbit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "all_tags")
data class Tag(
    @PrimaryKey(autoGenerate = false)
    val name: String,

) {
    constructor() : this("")
}