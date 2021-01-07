package com.larin_anton.rebbit.ui.profile

import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.repository.Repository
import javax.inject.Inject

class ProfileViewModel @Inject constructor(val repository: Repository): ViewModel() {

    private var currentUser : User? = null

    init {
        Application.application.appComponent.inject(this)
    }

    fun getCurrentUser(): User?{
        return currentUser
    }

}