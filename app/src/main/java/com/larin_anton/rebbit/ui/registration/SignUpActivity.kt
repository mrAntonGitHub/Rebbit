package com.larin_anton.rebbit.ui.registration

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.larin_anton.rebbit.repository.Repository
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class SignUpActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        Application.application.appComponent.inject(this)
        CoroutineScope(IO).launch {
            repository.setCurrentUser()
        }

        registration_signUpButton.setOnClickListener {
            onRegisterButtonClicked()
        }

    }

    private fun onRegisterButtonClicked(){
        registration_message.visibility = View.GONE
        registration_signUpButton.isEnabled = false
        when {
            registration_email.text?.toString()?.trim().isNullOrEmpty() -> {
                registration_email.error = "Введите почту"
                registration_signUpButton.isEnabled = true
            }
            registration_password.text?.toString()?.trim().isNullOrEmpty() -> {
                registration_password.error = "Введите пароль"
                registration_signUpButton.isEnabled = true
            }
            else -> {
                registration_progressBar.visibility = View.VISIBLE
                createFirebaseUser()
            }
        }
    }

    private fun createFirebaseUser() {
        CoroutineScope(IO).launch {
            val createUserResult = firebaseHelper.createNewFirebaseUser(registration_email.text.toString(), registration_password.text.toString())
            Log.e("AppDebug", "SignUpActivity:createFirebaseUser [${createUserResult}]")
            if (createUserResult.second == null) {
                startSignInActivity()
            } else {
                withContext(Main){
                    registration_message.text = createUserResult.first
                    registration_message.setTextColor(Color.RED)
                    registration_message.visibility = View.VISIBLE
                    registration_progressBar.visibility = View.GONE
                    registration_signUpButton.isEnabled = true
                }
            }
        }
    }

    private fun startSignInActivity(){
        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
        intent.putExtra("EMAIL",registration_email.text.toString())
        intent.putExtra("PASSWORD",registration_password.text.toString())
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}