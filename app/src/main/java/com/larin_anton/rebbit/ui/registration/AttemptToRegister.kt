package com.larin_anton.rebbit.ui.registration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.MainActivity
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.repository.FirebaseHelper
import kotlinx.android.synthetic.main.activity_attempt_to_register.*
import javax.inject.Inject

class AttemptToRegister : AppCompatActivity() {

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attempt_to_register)
        Application.application.appComponent.inject(this)


        offerToRegister_closeImage.setOnClickListener {
            signInAnAnonymously()
        }

        offerToRegister_skipButton.setOnClickListener {
            signInAnAnonymously()
        }

        offerToRegister_registerButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        offerToRegister_signInButton.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun signInAnAnonymously(){
        startActivity(Intent(this, MainActivity::class.java))
        firebaseHelper.signInAnAnonymously()
        finish()
    }
}