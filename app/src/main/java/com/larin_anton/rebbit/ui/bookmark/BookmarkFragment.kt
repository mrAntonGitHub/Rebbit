package com.larin_anton.rebbit.ui.bookmark

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.BookedPostsAdapter
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.interfaces.PublishedPostsDelegate
import com.larin_anton.rebbit.ui.dialogs.ComplainDialogFragment
import com.larin_anton.rebbit.utils.Utils
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookmarkFragment : Fragment(), PublishedPostsDelegate,
    ComplainDialogFragment.ComplainAboutAction {

    @Inject
    lateinit var bookmarkViewModel: BookmarkViewModel

    private val currentPosts = mutableListOf<RemotePost>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Application.application.appComponent.inject(this)
        return  inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BookedPostsAdapter()
        bookmarks_posts.adapter = adapter
        adapter.setDelegate(this)


        CoroutineScope(IO).launch {
            val posts = bookmarkViewModel.getBookmarksPosts()
            bookmarkViewModel.getCurrentUser()?.let { adapter.setCurrentUser(it) }
            withContext(Main){
                if (posts != null) {
                    adapter.submitList(posts)
                }
            }
        }
    }

    override suspend fun onComplainAboutClicked(remotePost: RemotePost) {
        if (isAdded){
            Utils.showDialog(remotePost, requireActivity(), this)
        }else{
            Log.e("AllStoriesFragment", "showDialog [Fragment is not attached to Activity]")
        }
    }

    override fun onLikeClicked(remotePost: RemotePost, isLikeSet: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun onCommentClicked(remotePost: RemotePost) {
        TODO("Not yet implemented")
    }

    override suspend fun onAuthorClicked(authorId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun onAddToBookmarksClicked(remotePost: RemotePost, isAddSet: Boolean) {

    }


    override suspend fun onSendComplainClicked(remotePost: RemotePost, complainType: String, complainDescription: String) {
        /* User wants to complain about the post */
        CoroutineScope(IO).launch {
//            val currentUserId = allStoriesViewModel.getCurrentUser()?.id
//            val complainPost = ComplainPost(currentUserId.toString(), remotePost.postId, complainType, complainDescription)
//            allStoriesViewModel.complainAboutPost(complainPost)
        }
    }

}