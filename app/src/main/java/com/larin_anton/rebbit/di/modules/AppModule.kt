package com.larin_anton.rebbit.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class AppModule @Inject constructor(private val application: Context) {

    @Provides
    @Singleton
    fun provideApplication(): Context = application

}