package com.larin_anton.rebbit

import android.app.Application
import com.larin_anton.rebbit.di.components.AppComponent
import com.larin_anton.rebbit.di.components.DaggerAppComponent
import com.larin_anton.rebbit.di.modules.AppModule
import com.larin_anton.rebbit.di.modules.DatabaseModule
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

private const val INIT_PREFERENCES = "INIT_PREFERENCES"

@Singleton
class Application @Inject constructor() : Application() {

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    lateinit var appComponent: AppComponent


    companion object {
        lateinit var application: com.larin_anton.rebbit.Application
        var firebaseUser: FirebaseUser? = null
    }



    override fun onCreate() {
        super.onCreate()
        application = this
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(applicationContext))
            .databaseModule(DatabaseModule(applicationContext))
            .build()

        application.appComponent.inject(this)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }


}