package com.larin_anton.rebbit.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.NotificationAdapter
import com.larin_anton.rebbit.data.model.LiveDataStatus
import com.larin_anton.rebbit.data.model.notifications.Notification
import com.larin_anton.rebbit.ui.comments.CommentsActivity
import com.larin_anton.rebbit.ui.home.allStories.POST_ID
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationsFragment @Inject constructor(): Fragment(),
    NotificationAdapter.OnNotificationAction {

    @Inject
    lateinit var notificationsViewModel: NotificationsViewModel

    val notifications = mutableListOf<Notification>()
    lateinit var adapter : NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Application.application.appComponent.inject(this)

        notificationsViewModel.notifications.observe(viewLifecycleOwner, {
            if (it.status == LiveDataStatus.SUCCESS){
                if (it.data != null){
                    if (::adapter.isInitialized){
                        updateAdapter(it.data)
                    }else{
                        initAdapter(it.data)
                    }
                    isRecyclerViewEmpty(adapter)
                }
            }
        })

    }

    private fun isRecyclerViewEmpty(adapter: NotificationAdapter) {
        if (adapter.itemCount == 0){
            notification_noStoriesAnimation.visibility = View.VISIBLE
            notification_bottomFirstPreviewTextView.visibility = View.VISIBLE
            notification_bottomSecondPreviewTextView.visibility = View.VISIBLE
        }
        else{
            notification_noStoriesAnimation.visibility = View.GONE
            notification_bottomFirstPreviewTextView.visibility = View.GONE
            notification_bottomSecondPreviewTextView.visibility = View.GONE
        }
    }

    private fun initAdapter(notifications: List<Notification>){
        this.notifications.clear()
        this.notifications.addAll(notifications)
        adapter = NotificationAdapter(this.notifications, this)
        notification_recyclerView.adapter = adapter
    }

    private fun updateAdapter(notifications: List<Notification>){
        this.notifications.clear()
        this.notifications.addAll(notifications)
        adapter.notifyDataSetChanged()
        if (notification_recyclerView.adapter == null){
            notification_recyclerView.adapter = adapter
        }

    }

    override fun onLikeSetUserClicked(userId: String) {
        Toast.makeText(requireContext(), "TODO: Open Profile", Toast.LENGTH_SHORT).show()
    }

    override fun onLikedPostClicked(postId: String) {
        val intent = Intent(requireContext(), CommentsActivity::class.java)
        intent.putExtra(POST_ID, postId)
        startActivity(intent)
    }

    override fun onRemoveNotificationClicked(notificationId: String) {
        CoroutineScope(IO).launch {
            notificationsViewModel.removeNotification(notificationId)
        }
    }


}