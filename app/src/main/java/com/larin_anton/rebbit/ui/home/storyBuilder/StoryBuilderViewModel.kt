package com.larin_anton.rebbit.ui.home.storyBuilder

import androidx.lifecycle.ViewModel
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.data.model.*
import com.larin_anton.rebbit.repository.FirebaseHelper
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.home.myStories.MyStoriesViewModel
import com.larin_anton.rebbit.utils.Utils
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryBuilderViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    @Inject
    lateinit var myStoriesViewModel: MyStoriesViewModel

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var utils: Utils

    init {
        Application.application.appComponent.inject(this)
    }

    // Get post for each post type
    suspend fun getPrivatePostAndNonChosenTags(postId: String) : Pair<LocalPost, Set<Tag>?>{
        val post = repository.getPrivatePost(postId.toLong())
        val tags = repository.getTags()
        val chosenAndNonChosenTags = if (tags != null){
            getNonChosenTags(post.listOfPostTags.toMutableSet() ,tags)
        }else { null }
        return Pair(post, chosenAndNonChosenTags)
    }

    suspend fun getOnModerationPostAndNonChosenTags(postId: String) : Pair<RemotePost?, Set<Tag>?>{
        val post = repository.getCurrentUserOnModerationPost(postId)
        val tags = repository.getTags()

        val listOfRemoteTags = post?.listOfTags?.toSet()
        val chosenAndNonChosenTags = if (listOfRemoteTags != null && tags != null){
            getNonChosenTags(listOfRemoteTags ,tags)
        }else { null }

        return Pair(post, chosenAndNonChosenTags)
    }

    private fun getNonChosenTags(postTags : Set<Tag>, allTags : Set<Tag>) : Set<Tag> {
        return  allTags.toMutableSet() - postTags
    }

    suspend fun getTags() : Set<Tag>?{
        return repository.getTags()
    }


    suspend fun addNewPrivatePost(post: LocalPost){
        repository.addPrivatePost(post)
        myStoriesViewModel.updateData()
    }

    suspend fun editPrivatePost(post: LocalPost){
        repository.editPrivatePost(post)
        myStoriesViewModel.updateData()
    }

    suspend fun editOnModerationPost(post: RemotePost){
        repository.editOnModerationPost(post)
        myStoriesViewModel.updateData()
    }

    fun editPublishedPost(){

    }

    fun isUserSet() : Boolean{
        return !(firebaseHelper.getCurrentFirebaseUser() == null || firebaseHelper.isCurrentUserAnonymous() == true)
    }

    //  +
    private fun setTags(chosenTags: List<String>, nonChosenTags: List<String>){
//        addStoryActivityActivity.setTags(chosenTags, nonChosenTags)
    }


    fun savePost(postToSave: LocalPost) {
//        CoroutineScope(IO).launch {
//            val listOfPostTags = mutableListOf<PostTag>()
//            if (isPostToEdit){
//                listOfChosenTags.forEach {
//                    listOfPostTags.add(PostTag(it, postToSave.postId.toString()))
//                }
//                postToSave.listOfPostTags = listOfPostTags
//                repository.editPrivatePost(postToSave)
//            }
//            else{
//                repository.addPersonalPosts(postToSave)
//            }
//            withContext(Main){
//                myStoriesViewModel.updateData()
//            }
//        }
    }

    suspend fun sharePost(remotePost: RemotePost) {
        repository.addOnModerationPost(remotePost)
        withContext(Main){
                myStoriesViewModel.updateData()
        }
    }



//    private fun List<Tag>.toTag(): MutableList<PostTag> {
//        val mutableList = mutableListOf<PostTag>()
//        this.forEach {
//            mutableList.add(PostTag(it.name))
//        }
//        return mutableList
//    }
}





