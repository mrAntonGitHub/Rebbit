package com.larin_anton.rebbit.ui.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.utils.Utils.Companion.isLastActionTimeLessThanDuration
import kotlinx.android.synthetic.main.fragment_complain_dialog.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

const val acceptDuration = 2_000L
const val ACCEPT = "Подтвердить"
const val SEND = "Отправить"

/* Dialog that give user opportunity to send report on post (to tell about some violations) */
class ComplainDialogFragment(
    private val remotePost: RemotePost,
    private val complainAboutAction: ComplainAboutAction
) : DialogFragment() {

    interface ComplainAboutAction{
        /* Communication interface */
        suspend fun onSendComplainClicked(remotePost: RemotePost, complainType: String, complainDescription: String)
    }

    // job to accept sending report
    private val acceptJob = CoroutineScope(IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dependency injection
        Application.application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_complain_dialog, container, false)

        // send/accept colors and icons
        val sendColor = R.color.colorAccent
        val sendIcon = R.drawable.ic_send
        val acceptColor = R.color.acceptColor
        val acceptIcon = R.drawable.ic_check

        // Buttons
        val send = view.findViewById<Button>(R.id.complain_violation_send_btn)
        val cancel = view.findViewById<Button>(R.id.complain_violation_cancel_btn)
        // Spinner
        val violationType = view.findViewById<Spinner>(R.id.complain_violation_sp)

        // Set spinner (data from strings.array)
        val adapter = context?.let { ArrayAdapter.createFromResource(it, R.array.violation, R.layout.support_simple_spinner_dropdown_item) }
        adapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        violationType.adapter = adapter


        var lastPress : Long = 0
        send.setOnClickListener {
            val currentTime: Long = System.currentTimeMillis()
            acceptJob.launch {
                withContext(Main){
                    if (isLastActionTimeLessThanDuration(lastPress, acceptDuration)) {
                        complainAboutAction.onSendComplainClicked(remotePost, violationType.selectedItem.toString(), complain_violation_description.text.toString())
                        dialog?.dismiss()
                    } else {
                        send.text = ACCEPT
                        it.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, acceptColor, null))
                        val drawable = context?.let { it1 -> ContextCompat.getDrawable(it1, acceptIcon) }
                        send.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                        lastPress = currentTime
                    }
                }
                delay(acceptDuration)
                withContext(Main){
                    send.text = SEND
                    it.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, sendColor, null))
                    val drawable = context?.let { it1 -> ContextCompat.getDrawable(it1, sendIcon) }
                    send.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                }
            }
        }

        cancel.setOnClickListener {
            dialog?.dismiss()
        }

        return view

    }

    override fun onDestroy() {
        super.onDestroy()
        acceptJob.cancel()
    }
}