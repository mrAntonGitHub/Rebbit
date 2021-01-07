package com.larin_anton.rebbit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.larin_anton.rebbit.data.local.dao.LocalDao
import com.larin_anton.rebbit.data.model.PostTag
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.LocalPost


@Database(entities = [LocalPost::class, PostTag::class, Tag::class], version = 1)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun localPostDao(): LocalDao
}