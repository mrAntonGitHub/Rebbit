package com.larin_anton.rebbit.ui.home.myStories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.MyPostsAdapter
import com.larin_anton.rebbit.data.model.*
import com.larin_anton.rebbit.ui.home.storyBuilder.StoryBuilderActivity
import kotlinx.android.synthetic.main.fragment_my_stories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject

class MyStoriesFragment : Fragment(), MyPostsAdapter.OnMyPostsActions{
    @Inject
    lateinit var myStoriesViewModel: MyStoriesViewModel

    var currentListOfUserPrivatePosts = listOf<LocalPost>()
    var currentListOfUserOnModerationPosts = listOf<RemotePost>()
    var currentListOfUserPublishedPosts = listOf<RemotePost>()

    var currentListOfPosts = mutableListOf<MyPosts>()
    var currentRecyclerViewState : PostType = PostType.ALL

    //  Adapters for recyclerView
    private lateinit var postAdapter : MyPostsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Application.application.appComponent.inject(this)
        return inflater.inflate(R.layout.fragment_my_stories, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CoroutineScope(IO).launch {
            myStoriesViewModel.updateData()
        }

        myStoriesViewModel.currentRecyclerViewState.observe(viewLifecycleOwner, {
            if (it?.status == LiveDataStatus.SUCCESS) {
                when (it.data) {
                    PostType.PRIVATE -> {
                        Log.d("MyStoriesFragment", "onActivityCreated [Private posts]")
                        currentRecyclerViewState = PostType.PRIVATE
                        setAdapter(PostType.PRIVATE)
                    }
                    PostType.PUBLISHED -> {
                        Log.d("MyStoriesFragment", "onActivityCreated [Published posts]")
                        currentRecyclerViewState = PostType.PUBLISHED
                        setAdapter(PostType.PUBLISHED)
                    }
                    PostType.MODERATION -> {
                        Log.d("MyStoriesFragment", "onActivityCreated [Moderation posts]")
                        currentRecyclerViewState = PostType.MODERATION
                        setAdapter(PostType.MODERATION)
                    }
                    PostType.ALL -> {
                        Log.d("MyStoriesFragment", "onActivityCreated [All posts]")
                        currentRecyclerViewState = PostType.ALL
                        setAdapter(PostType.ALL)
                    }
                }
            }
        })

        myStoriesViewModel.listOfUserPrivatePosts.observe(viewLifecycleOwner){
            if (it.status == LiveDataStatus.SUCCESS){
                if (it.data != null){
                    currentListOfUserPrivatePosts = it.data
                    Log.e("MyStoriesFragment", "onActivityCreated [${currentListOfUserPrivatePosts}]")
                    updateAdapter(PostType.PRIVATE)
                }
            }
        }

        myStoriesViewModel.listOfUserPublishedPosts.observe(viewLifecycleOwner){

        }

        myStoriesViewModel.listOfUserOnModerationPosts.observe(viewLifecycleOwner){
            if (it.status == LiveDataStatus.SUCCESS){
                if (it.data != null){
                    currentListOfUserOnModerationPosts = it.data
                    Log.e("MyStoriesFragment", "updateAdapter [${currentListOfUserOnModerationPosts.size}]")
                    updateAdapter(PostType.MODERATION)
                }
            }
        }

        myStories_updateData.setOnRefreshListener {
            CoroutineScope(IO).launch {
                myStoriesViewModel.updateData()
            }
        }

    }

    private fun setAdapter(postType: PostType){
        var currentList = listOf<MyPosts>()
        when(postType){
            PostType.PRIVATE -> {
                currentList = currentListOfUserPrivatePosts.map {localPost ->
                    MyPosts(PostType.PRIVATE, localPost)
                }
            }
            PostType.MODERATION -> {
                currentList = currentListOfUserOnModerationPosts.map { remotePost ->
                    MyPosts(PostType.MODERATION, remotePost)
                }
            }
            PostType.PUBLISHED -> {

            }
            PostType.ALL -> {
                currentList =
                    currentListOfUserPrivatePosts.map {
                            localPost -> MyPosts(PostType.PRIVATE, localPost) } + currentListOfUserOnModerationPosts.map {
                            remotePost -> MyPosts(PostType.MODERATION, remotePost) } + currentListOfUserPublishedPosts.map {
                            remotePost -> MyPosts(PostType.PUBLISHED, remotePost) }
            }
        }
        currentListOfPosts.clear()
        currentListOfPosts.addAll(sortListOfPostsByDate(currentList.toMutableList()))
        if (!::postAdapter.isInitialized){
            postAdapter = MyPostsAdapter(currentListOfPosts, this)
            myStories_postsRecyclerView.adapter = postAdapter
        }else{
            postAdapter.notifyDataSetChanged()
            if (myStories_postsRecyclerView.adapter == null){
                myStories_postsRecyclerView.adapter = postAdapter
            }
            myStories_postsRecyclerView.scrollToPosition(currentListOfPosts.size - 1)
        }

        checkAdapterItemSize()
    }

    private fun sortListOfPostsByDate(currentListOfPosts : MutableList<MyPosts>) : List<MyPosts>{
        currentListOfPosts.sortBy {
            Log.e("MyStoriesFragment1", "sortListOfPostsByDate [AB = ${timeSelector(it)}]")
            timeSelector(it)
        }
        Log.e("MyStoriesFragment1", "sortListOfPostsByDate [A = ${currentListOfPosts}]")
        return currentListOfPosts
    }

    private fun timeSelector(it: MyPosts) : String?{
        return when(it.TYPE){
            PostType.PRIVATE ->{
                Log.e("MyStoriesFragment1", "timeSelector [1]")
                (it.post as LocalPost).published_time
            }
            PostType.PUBLISHED, PostType.MODERATION->{
                Log.e("MyStoriesFragment1", "timeSelector [2]")
                (it.post as RemotePost).published_time
            }
            else -> {
                Log.e("MyStoriesFragment", "sortListOfPostsByDate [Unexpected post type]")
                null
            }
        }
    }

    private fun updateAdapter(postType: PostType){
        if (currentRecyclerViewState == postType){
            myStories_updateData.isRefreshing = false
            setAdapter(postType)
        }else if(currentRecyclerViewState == PostType.ALL){
            myStories_updateData.isRefreshing = false
            setAdapter(PostType.ALL)
        }
    }

    private fun checkAdapterItemSize() {
        if (postAdapter.itemCount > 0) {
            showOrHideBackgroundItems(false)
        } else {
            showOrHideBackgroundItems(true)
        }
    }
    private fun showOrHideBackgroundItems(isRecyclerViewEmpty: Boolean) {
        Log.e("MyStoriesFragment", "showOrHideNoPrivateStoryIcons [${isRecyclerViewEmpty}]")
        if (isRecyclerViewEmpty) {
            myStories_topPreviewTextView.text = "Пока тут ничего нет\n Напишите Вашу первую историю!"
            myStories_topPreviewTextView.visibility = View.VISIBLE

            myStories_bottomFirstPreviewTextView.text = "В этом разделе будут отображаться все ваши опубликованные и неопубликованные истории"
            myStories_bottomFirstPreviewTextView.visibility = View.VISIBLE

            myStories_bottomSecondPreviewTextView.text = "Доступ к неопубликованным историям есть только у Вас"
            myStories_bottomSecondPreviewTextView.visibility = View.VISIBLE
            myStories_noStoriesAnimation.visibility = View.VISIBLE
        } else {
            myStories_topPreviewTextView.visibility = View.GONE
            myStories_bottomFirstPreviewTextView.visibility = View.GONE
            myStories_bottomSecondPreviewTextView.visibility = View.GONE
            myStories_noStoriesAnimation.visibility = View.GONE
        }
    }


    override fun onDeleteClicked(localPost: LocalPost) {
        myStoriesViewModel.onDeleteClicked(localPost)
    }

    override fun onPostClicked(postId: Long) {
        val intent = Intent(requireContext(), StoryBuilderActivity::class.java)
        intent.putExtra(StoryBuilderActivity.POST_TYPE, StoryBuilderActivity.PRIVATE_POST)
        intent.putExtra(StoryBuilderActivity.PRIVATE_POST_ID, postId.toString())
        startActivity(intent)
    }

    override fun onPublishClicked(localPost: LocalPost) {
        myStoriesViewModel.onPublishClicked(localPost)
    }

    override fun onEditOnModerationPostClicked(remotePost: RemotePost) {
        val intent = Intent(requireContext(), StoryBuilderActivity::class.java)
        intent.putExtra(StoryBuilderActivity.POST_TYPE, StoryBuilderActivity.ON_MODERATION_POST)
        intent.putExtra(StoryBuilderActivity.ON_MODERATION_POST_ID, remotePost.postId)
        startActivity(intent)
    }

    override fun onCancelModerating(remotePost: RemotePost) {
        myStoriesViewModel.onCancelModerating(remotePost)
    }

    override fun onDeletePublishedPostClicked(remotePost: RemotePost) {
        myStoriesViewModel.onDeletePublishedPostClicked(remotePost)
    }
}