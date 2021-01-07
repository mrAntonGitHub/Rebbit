package com.larin_anton.rebbit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.Comment
import com.larin_anton.rebbit.utils.DateFormat
import com.larin_anton.rebbit.utils.Utils.Companion.millisecondsToStringPattern

// TODO Refactoring REQUIRE
class CommentsAdapter(private val comments: List<Comment>, private val onCommentAction: OnCommentAction) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    interface OnCommentAction{
        fun complainOnComment(comment: Comment)
        fun removeComment(comment: Comment)
        fun editComment(comment: Comment)
        fun onReply(comment: Comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = comments[position]
        holder.apply {
            userName.text = item.userName
            commentText.text = item.commentText
            time.text = item.time.millisecondsToStringPattern(DateFormat.DayMonthTimeWithSeconds)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.comments_author)
        val userIcon: ImageView = view.findViewById(R.id.comments_userIcon)
        val commentText: TextView = view.findViewById(R.id.comments_body)
        val time: TextView = view.findViewById(R.id.comments_publishedTime)
        val moreButton : Button = view.findViewById(R.id.comments_moreButton)

        init {
            moreButton.setOnClickListener {
                showPopMenu(it, adapterPosition)
            }
        }
    }

    fun showPopMenu(it: View, adapterPosition: Int){
        val popupMenu = PopupMenu(it.context, it)
        popupMenu.apply {
            menuInflater.inflate(R.menu.menu_comment, popupMenu.menu)
            setOnMenuItemClickListener { menuItem ->
                when (menuItem?.itemId) {
                    R.id.comment_menu_remove -> {
                        onCommentAction.removeComment(comments[adapterPosition])
                    }
                    R.id.comment_menu_edit -> {
                        onCommentAction.editComment(comments[adapterPosition])
                    }
                    R.id.comment_menu_complain -> {
                        onCommentAction.complainOnComment(comments[adapterPosition])
                    }
                    R.id.comment_menu_reply -> {
                        onCommentAction.onReply(comments[adapterPosition])
                    }
                }
                false
            }
            show()
    }
}




}