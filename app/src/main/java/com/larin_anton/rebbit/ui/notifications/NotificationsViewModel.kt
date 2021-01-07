package com.larin_anton.rebbit.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.notifications.Notification
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.utils.LiveDataWrapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var repository: Repository

    init {
        Application.application.appComponent.inject(this)
    }

    val notifications : LiveData<LiveDataWrapper<List<Notification>>> = repository.notifications


    suspend fun removeNotification(notificationId: String){
        repository.removeLikeNotification(notificationId)
    }


}