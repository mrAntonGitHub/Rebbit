package com.larin_anton.rebbit.utils

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.FragmentActivity
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.interfaces.PublishedPostsDelegate
import com.larin_anton.rebbit.ui.dialogs.ComplainDialogFragment
import com.larin_anton.rebbit.ui.home.allStories.DIALOG_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Utils @Inject constructor(){

    fun createDialogSaveNote(title: String?, message: String?, positive: Pair<String, () -> Unit> , negative: Pair<String, () -> Unit>, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(positive.first) { dialog, which ->
            positive.second()
        }

        builder.setNegativeButton(negative.first) { dialog, which ->
            negative.second()
        }

        builder.show()
    }

    companion object{
        // Convert milliseconds to String Pattern
        fun String.millisecondsToStringPattern(pattern: DateFormat) : String{
            val dv = this.toLong()*1000
            val df = Date(dv)
            return SimpleDateFormat(pattern.pattern, Locale.getDefault()).format(df)
        }

        fun isLastActionTimeLessThanDuration(lastAction: Long, duration: Long) : Boolean{
            /* Compare last action with current time and return true if value less than duration */
            val currentTime: Long = System.currentTimeMillis()
            return currentTime - lastAction < duration
        }

        fun estimatedTimeToRead(text: String, context: Context): String{

            fun countTimeToRead(text: String): Int{
                val countOfWords =  text.trim().split("\\s+".toRegex()).size
                return countOfWords / 150
            }

            return when (val time = countTimeToRead(text)){
                0 -> "Займет менее минуты"
                else -> {
                    context.resources.getQuantityString(R.plurals.time, time, time)
                }
            }
        }

        fun createPopupMenu(view: View, remotePost: RemotePost, callback: PublishedPostsDelegate){

            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_published_post, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem?.itemId) {
                    R.id.post_menu_complain -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            callback.onComplainAboutClicked(remotePost)
                        }
                    }
                }
                false
            }
            popupMenu.show()
        }

        fun showDialog(remotePost: RemotePost, activity: FragmentActivity, complainAboutAction: ComplainDialogFragment.ComplainAboutAction) {
            /* Show on complain dialog and handle on "send complain" click action through the interface  */
            val fragment = ComplainDialogFragment(remotePost, complainAboutAction)
            fragment.show(activity.supportFragmentManager, DIALOG_NAME)

        }
    }

    fun Long.toNormalTime(dateFormat : DateFormat) : String{
        val simpleDateFormat = SimpleDateFormat(dateFormat.pattern)
        return simpleDateFormat.format(this*1000)
    }

    // Getting UNIX time
    fun getUnixTime() : String{
        val unixTime = System.currentTimeMillis()/1000
        Log.e("Utils.PROJECT_NAME", unixTime.toString())
        return unixTime.toString()
    }

    // Convert milliseconds to Date
    fun String.millisecondsToDate(): Date{
        Log.e("Utils.PROJECT_NAME", Date(this.toLong()*1000).toString())
        return Date(this.toLong()*1000)
    }


}