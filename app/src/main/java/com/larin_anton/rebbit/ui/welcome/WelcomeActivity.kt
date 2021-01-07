package com.larin_anton.rebbit.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.MainActivity
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.registration.AttemptToRegister
import com.larin_anton.rebbit.utils.Settings
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject


class WelcomeActivity : AppCompatActivity() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        Application.application.appComponent.inject(this)

        if (!settings.isFirstStart(this)) chooseNextActivity()

        welcomeActivity_skipButton.setOnClickListener {
            settings.setFirstStart(false, this)
            chooseNextActivity()
        }
    }

    private fun chooseNextActivity() {
        val isCurrentUserAnonymous = firebaseHelper.isCurrentUserAnonymous()
        if (isCurrentUserAnonymous != null){
            when(isCurrentUserAnonymous){
                true -> {
                    startActivity(Intent(this, AttemptToRegister::class.java))
                }
                false -> {
                    CoroutineScope(IO).launch {
                        repository.replaceCurrentUserRemotePrivatePosts()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
        else{
            startActivity(Intent(this, AttemptToRegister::class.java))
        }
        finish()
    }
}