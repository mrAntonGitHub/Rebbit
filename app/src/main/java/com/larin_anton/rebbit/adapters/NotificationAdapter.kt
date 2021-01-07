package com.larin_anton.rebbit.adapters

import android.content.res.ColorStateList
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.Comment
import com.larin_anton.rebbit.data.model.notifications.Notification
import com.larin_anton.rebbit.data.model.notifications.NotificationLike
import com.larin_anton.rebbit.data.model.notifications.NotificationType
import com.larin_anton.rebbit.ui.dialogs.acceptDuration
import com.larin_anton.rebbit.utils.DateFormat
import com.larin_anton.rebbit.utils.Utils.Companion.isLastActionTimeLessThanDuration
import com.larin_anton.rebbit.utils.Utils.Companion.millisecondsToStringPattern
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

const val connectionText = "оценил(-а) Вашу запись от"
const val acceptDuration = 2_000L

// TODO Refactoring REQUIRE
class NotificationAdapter(
    private val listOfNotification: List<Notification>,
    val onNotificationAction: OnNotificationAction
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val acceptJob = CoroutineScope(IO)

    interface OnNotificationAction {
        fun onLikeSetUserClicked(userId: String)
        fun onLikedPostClicked(postId: String)


        fun onRemoveNotificationClicked(notificationId: String)
    }

    private val notificationLike = 1
    private val notificationComment = 2
    private val notificationComplain = 3


    override fun getItemCount(): Int {
        return listOfNotification.size
    }

    override fun getItemViewType(position: Int): Int {
        val notification = listOfNotification[position]
        return when (notification.TYPE) {
            NotificationType.LIKE -> {
                notificationLike
            }
            NotificationType.COMMENT -> {
                notificationComment
            }
            NotificationType.COMPLAIN -> {
                notificationComplain
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            notificationLike -> {
                ViewHolderLike(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notification,
                        parent,
                        false
                    )
                )
            }
            notificationComment -> {
                ViewHolderComment(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notification,
                        parent,
                        false
                    )
                )
            }
            notificationComplain -> {
                ViewHolderLike(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notification,
                        parent,
                        false
                    )
                )
            }
            else -> {
                ViewHolderLike(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notification,
                        parent,
                        false
                    )
                )
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            notificationLike -> {
                setLayoutLike(holder as ViewHolderLike, position)
            }
            notificationComment -> {
                setLayoutComment(holder as ViewHolderComment, position)
            }
            notificationComplain -> {
                setLayoutComplain()
            }
            else -> {
            }
        }
    }

    private fun setLayoutLike(viewHolder: ViewHolderLike, position: Int) {
        val likeNotification = (listOfNotification[position].notification) as NotificationLike

        val likedTime =
            likeNotification.time.millisecondsToStringPattern(DateFormat.DayMonthTimeWithoutSeconds)
        val likedByUserName = likeNotification.likedByName
        val likedByIcon = likeNotification.likedByIcon
        val postPublishedTime =
            likeNotification.post.published_time.millisecondsToStringPattern(DateFormat.DayMonthTimeWithoutSeconds)
        val postBeginning = likeNotification.post.body.slice(0..70) + "..."

        viewHolder.typeIcon.background.setTint(viewHolder.heartColor)
        val resultText = "$likedByUserName $connectionText $postPublishedTime. $postBeginning"

        val highlightedText: List<HashMap<String, View.OnClickListener>> = listOf(
            hashMapOf(
                Pair(likedByUserName, object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        onLikedUserClicked(likeNotification.likedById)
                    }
                })
            ), hashMapOf(Pair(postBeginning, object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    onPostClicked(likeNotification.postId)
                }
            }))
        )

        val highlightColor = viewHolder.itemView.context.getColor(R.color.main_primaryColor)
        val linkedText = makeLinks(resultText, highlightedText, highlightColor)

        viewHolder.body.movementMethod = LinkMovementMethod.getInstance()
        viewHolder.body.text = linkedText

        viewHolder.likedTime.text = likedTime
    }

    private fun setLayoutComment(viewHolder: ViewHolderComment, position: Int) {
        viewHolder.typeIcon.setImageResource(R.drawable.ic_comment)
        viewHolder.typeIcon.background.setTint(viewHolder.itemView.context.resources.getColor(R.color.main_darkGreen))

        val commentBy = ((listOfNotification[position].notification) as Comment).userName

//        val postTitle = listOfNotification[position].post.title
//        val postStart : String = if (postTitle.isNullOrEmpty()){
//            listOfNotification[position].post.body
//        }else{
//            postTitle
//        }
        val highlightColor = viewHolder.itemView.context.getColor(R.color.main_primaryColor)

//        val res : List<HashMap<String, View.OnClickListener>> = listOf(
//            hashMapOf(
//                Pair(postStart, object : View.OnClickListener {
//                    override fun onClick(p0: View?) {
//                        Toast.makeText(
//                            viewHolder.itemView.context,
//                            postStart,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                })
//            ), hashMapOf(Pair(commentBy, object : View.OnClickListener {
//                override fun onClick(p0: View?) {
//                    Toast.makeText(viewHolder.itemView.context, commentBy, Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }))
//        )

//        val string = makeLinks(notification, res, highlightColor)

        viewHolder.body.movementMethod = LinkMovementMethod.getInstance()
//        viewHolder.body.setText(string, TextView.BufferType.SPANNABLE)

//        viewHolder.likedTime.text = listOfNotification[position].actionTime

    }

    private fun setLayoutComplain() {

    }


    inner class ViewHolderLike(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userIcon: ImageView = itemView.findViewById(R.id.itemNotification_typeIcon)
        val body: TextView = itemView.findViewById(R.id.itemNotification_body)
        val likedTime: TextView = itemView.findViewById(R.id.itemNotification_likedTime)
        val typeIcon: ImageView = itemView.findViewById(R.id.itemNotification_typeIcon)
        private val removeNotification: ImageView =
            itemView.findViewById(R.id.itemNotification_remove)

        val heartColor = ContextCompat.getColor(itemView.context, android.R.color.holo_red_light)

        private val plainColor = ContextCompat.getColor(itemView.context, android.R.color.white)
        private val accentColor = heartColor

        init {
            removeNotification.setOnClickListener {
                onRemoveClicked((listOfNotification[adapterPosition].notification as NotificationLike).likeId, it, accentColor, plainColor)
            }
        }
    }

    var lastPress: Long = 0
    private fun onRemoveClicked(notificationId: String, view: View, acceptColor: Int, plainColor: Int) {
        val currentTime: Long = System.currentTimeMillis()
        val originalImageColor = (view as ImageView).imageTintList
        acceptJob.launch {
            withContext(Dispatchers.Main) {
                if (isLastActionTimeLessThanDuration(lastPress, acceptDuration)) {
                    onNotificationAction.onRemoveNotificationClicked(notificationId)
                } else {
                    view.imageTintList = ColorStateList.valueOf(plainColor)
                    view.backgroundTintList = ColorStateList.valueOf(acceptColor)
                    lastPress = currentTime
                }
            }
            delay(acceptDuration)
            withContext(Dispatchers.Main) {
                view.imageTintList = originalImageColor
                view.backgroundTintList = ColorStateList.valueOf(plainColor)
            }
        }
    }

    fun onLikedUserClicked(userId: String) {
        onNotificationAction.onLikeSetUserClicked(userId)
    }

    fun onPostClicked(postId: String) {
        onNotificationAction.onLikedPostClicked(postId)
    }

    inner class ViewHolderComment(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemNotification_typeIcon)
        val body: TextView = itemView.findViewById(R.id.itemNotification_body)
        val likedTime: TextView = itemView.findViewById(R.id.itemNotification_likedTime)
        val typeIcon: ImageView = itemView.findViewById(R.id.itemNotification_typeIcon)

    }

    inner class ViewHolderComplain(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    private fun makeLinks(text: String, listOfHashMap: List<HashMap<String, View.OnClickListener>>, phraseColor: Int): SpannableString {
        val spannableString = SpannableString(text)
        listOfHashMap.forEach { hashMap ->
            hashMap.forEach {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = phraseColor // you can use custom color
                        ds.isUnderlineText = false // this remove the underline
                    }

                    override fun onClick(view: View) {
                        it.value.onClick(view)
                    }
                }
                val start = text.indexOf(it.key)
                val end = start + it.key.length
                Log.e("NotificationAdapter", "makeLinks [${start} ${end}]")
                spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return spannableString
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Log.e("NotificationAdapter", "onViewDetachedFromWindow [10]")
        lastPress = 0
    }

}