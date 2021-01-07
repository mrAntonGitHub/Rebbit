package com.larin_anton.rebbit.ui.registration

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.MainActivity
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.data.model.UserStatus
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.utils.Settings
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val INIT_PREFERENCES = "INIT_PREFERENCES"

class SignInActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        Application.application.appComponent.inject(this)

        var shouldCreateUserNode = false

        if (intent.extras != null){
            CoroutineScope(IO).launch {
                firebaseHelper.verifyUserEmail()
            }
            shouldCreateUserNode = true
            signIn_email.setText(intent.extras!!.getString("EMAIL", ""))
            signIn_password.setText(intent.extras!!.getString("PASSWORD", ""))
            signIn_message.text = "По указанному адресу выслана ссылка для подтверждения адреса электронной почты."
            signIn_message.setTextColor(Color.BLACK)
            signIn_message.visibility = View.VISIBLE
        }

        signIn_signInButton.setOnClickListener {
            signIn_progressBar.visibility = View.VISIBLE
            signIn_message.visibility = View.GONE
            signIn_signInButton.isEnabled = false
            when {
                signIn_email.text?.toString()?.trim().isNullOrEmpty() -> {
                    signIn_email.error = "Введите почту"
                    signIn_progressBar.visibility = View.GONE
                    signIn_signInButton.isEnabled = true
                }
                signIn_password.text?.toString()?.trim().isNullOrEmpty() -> {
                    signIn_password.error = "Введите пароль"
                    signIn_progressBar.visibility = View.GONE
                    signIn_signInButton.isEnabled = true
                }
                else -> {
                    CoroutineScope(IO).launch {
                        val signInResult = firebaseHelper.signInWithEmailAndPassword(signIn_email.text.toString(), signIn_password.text.toString())
                        if (signInResult.second == null){
                            if (firebaseHelper.isUserEmailVerified()){
                                if (shouldCreateUserNode){
                                    val userId = firebaseHelper.getCurrentUserId()
                                    if (userId != null){
                                        val user = User(userId, signIn_email.text.toString(), UserStatus.Plain, "", signIn_email.text.toString(), "", "0", "false", null, null,null, null, null, null)
                                        repository.addNewUser(user)
                                    }
                                }
                                repository.replaceCurrentUserLocalPrivatePosts()
                                withContext(Main){
                                    repository.setCurrentUser()
                                    startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                                    finish()
                                }
                            }
                            else{
                                withContext(Main){
                                    signIn_message.text = "Перейдите по ссылке в письме и подтвердите адрес вашей почты!"
                                    signIn_message.setTextColor(Color.RED)
                                    signIn_message.visibility = View.VISIBLE
                                    signIn_progressBar.visibility = View.GONE
                                    signIn_signInButton.isEnabled = true
                                }
                            }
                        }else{
                            withContext(Main){
                                signIn_message.text = signInResult.first
                                signIn_message.setTextColor(Color.RED)
                                signIn_message.visibility = View.VISIBLE
                                signIn_progressBar.visibility = View.GONE
                                signIn_signInButton.isEnabled = true
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}