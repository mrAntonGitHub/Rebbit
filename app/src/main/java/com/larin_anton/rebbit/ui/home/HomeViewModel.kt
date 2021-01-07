package com.larin_anton.rebbit.ui.home

import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.PostType
import com.larin_anton.rebbit.data.model.User
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.home.myStories.MyStoriesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeViewModel @Inject constructor(): ViewModel() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var myStoriesViewModel: MyStoriesViewModel

    var firstTagName = "Истории"
    var secondTagName = "Мои истории"

    var personLiveData : User? = null

    init {
        Application.application.appComponent.inject(this)
        CoroutineScope(IO).launch {
            val user = repository.getCurrentUser()
            withContext(Main){
                personLiveData = user
            }
        }
    }

    fun changeStoryType(type: Int){
        when (type) {
            R.id.menu_myStories_publishedPosts -> {
                myStoriesViewModel.setCurrentRecyclerViewType(PostType.PUBLISHED)
            }
            R.id.menu_myStories_onModerationPosts -> {
                myStoriesViewModel.setCurrentRecyclerViewType(PostType.MODERATION)
            }
            R.id.menu_myStories_allStories -> {
                myStoriesViewModel.setCurrentRecyclerViewType(PostType.ALL)
            }
            R.id.menu_myStories_privatePosts -> {
                myStoriesViewModel.setCurrentRecyclerViewType(PostType.PRIVATE)
            }
        }
    }


}