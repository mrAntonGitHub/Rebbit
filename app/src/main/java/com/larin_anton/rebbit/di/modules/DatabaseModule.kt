package com.larin_anton.rebbit.di.modules

import android.content.Context
import androidx.room.Room
import com.larin_anton.rebbit.data.local.LocalDatabase
import com.larin_anton.rebbit.data.local.dao.LocalDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule(private val application: Context) {

    @Singleton
    @Provides
    fun provideLocalDatabase(): LocalDatabase {
        return Room.databaseBuilder(application, LocalDatabase::class.java, "localPosts.db")
            .build()
    }

    @Singleton
    @Provides
    fun provideLocalPostDao(localDatabase: LocalDatabase): LocalDao {
        return localDatabase.localPostDao()
    }

}