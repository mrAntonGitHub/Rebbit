package com.larin_anton.rebbit.ui.profile

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.registration.AttemptToRegister
import com.larin_anton.rebbit.utils.Settings
import com.larin_anton.rebbit.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_profile_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ProfileFragment : Fragment() {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var repository: Repository


    @Inject
    lateinit var profileViewModel: ProfileViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Application.application.appComponent.inject(this)

        val currentUser = firebaseHelper.getCurrentFirebaseUser()
        if (currentUser != null){
            val currentFirebaseUser = firebaseHelper.isCurrentUserAnonymous()
            if (currentFirebaseUser != null){
                if (currentFirebaseUser){
                    startAttemptToRegisterActivity()
                }
            }
        }else{
            startAttemptToRegisterActivity()
        }

    }

    private fun startAttemptToRegisterActivity(){
        val intent = Intent(requireContext(), AttemptToRegister::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        changeStatusBarColor(R.color.main_primaryColor)
//        val contextThemeWrapper: Context = ContextThemeWrapper(activity, R.style.ProfileTheme)
//        val localInflater = inflater.cloneInContext(contextThemeWrapper)
//        return localInflater.inflate(R.layout.fragment_profile, container, false)
        return layoutInflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(IO).launch {
            Log.e("AppDebug", "ProfileFragment:onViewCreated [${repository.getCurrentUser()}]")
            val user = repository.getCurrentUser()
            Log.d("AppDebug", "ProfileFragment:onViewCreated [${user}]")
            withContext(Main){
                user?.apply{
                    profile_userNick.text = name
                    profile_userPrivateStatus.text = "Тут будет статус пользователя"
                    profile_userName.setText(name)
                    profile_userEmail.setText(email)
                    profile_userPhoneNumber.setText(phoneNumber)
                    profile_userStatus.setText(status.toString())
                }
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setSoftKeyBoard()

        val firebaseAuth = FirebaseAuth.getInstance()
        profile_signOut.setOnClickListener {
            firebaseAuth.signOut()
            showDialogBeforeSwitchOff()
        }
    }


fun updateStatusBarColor(color: Int) {
    val window: Window = requireActivity().window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
}

    private fun changeStatusBarColor(color: Int){
        requireActivity().getColor(color).let {
            requireActivity().window?.setStatusBarColor(it)
        }
    }

    private fun setSoftKeyBoard(){
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

private fun showDialogBeforeSwitchOff() {
    utils.createDialogSaveNote(
        null,
        "Приложение закроется и Вам нужно будет открыть его повторно",
        Pair(
            "Ок",
            {
                CoroutineScope(IO).launch {
                    repository.signOut()
                    withContext(Main){
                        (requireContext().getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                    }
                }
            }),
        Pair("Отмена", {}),
        requireContext()
    )
}
}

