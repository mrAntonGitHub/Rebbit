package com.larin_anton.rebbit.ui.home.storyBuilder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import co.lujun.androidtagview.TagView
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.data.model.Tag
import com.larin_anton.rebbit.data.model.LocalPost
import com.larin_anton.rebbit.data.model.RemotePost
import com.larin_anton.rebbit.ui.registration.AttemptToRegister
import com.larin_anton.rebbit.utils.Utils
import kotlinx.android.synthetic.main.activity_add_story.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val EDIT_POST = "Редактирование"
const val CREATING_POST = "Создание поста"

class StoryBuilderActivity : AppCompatActivity() {
    
    companion object{
        const val POST_TYPE = "PostType"
        const val PRIVATE_POST = "PrivatePost"
        const val ON_MODERATION_POST = "ModerationPost"
        const val PUBLISHED_POST = "PublishedPost"

        const val PRIVATE_POST_ID = "PrivatePostID"
        const val ON_MODERATION_POST_ID = "OnModerationPostID"
        const val PUBLISHED_POST_ID = "PublishedPostID"
        const val ID = "PostID"
    }

    @Inject
    lateinit var storyBuilderViewModel: StoryBuilderViewModel

    @Inject
    lateinit var utils: Utils

    // Show current post type (if null -> new post, if PRIVATE_POST -> private post and etc)
    private var currentPostType : String? = null

    //  List of chosen / not chosen tags
    private var listOfChosenTags = mutableSetOf<Tag>()
    private var listOfNonChosenTags = mutableSetOf<Tag>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)
        Application.application.appComponent.inject(this)

        editPostOrCreate()
    }

    private fun editPostOrCreate(){
        if (intent.extras != null) {
            // Load post to edit
            supportActionBar?.title = EDIT_POST
            loadPost()
        }else {
            supportActionBar?.title = CREATING_POST
            //  Get tags from repository and show them
            if (!storyBuilderViewModel.isUserSet()){
                storyBuilderViewModel.utils.createDialogSaveNote(null, "Вы не авторизовались в системе и не можете публиковывать истории, только сохранять их на устройстве", Pair("Зарегистрироваться", {
                    startAttemptToRegisterActivity()
                }), Pair("Продолжить", {}), this)
            }
            CoroutineScope(IO).launch {
                val tags = storyBuilderViewModel.getTags()
                withContext(Main) {

                    listOfNonChosenTags = tags?.toMutableSet() ?: mutableSetOf()
                    setTags()
                }
            }
        }
    }

    private fun loadPost(){
        when(intent.getStringExtra(POST_TYPE)){
            PRIVATE_POST -> {
                currentPostType = PRIVATE_POST
                loadPrivatePost(intent)
            }
            ON_MODERATION_POST -> {
                currentPostType = ON_MODERATION_POST
                loadOnModerationPost(intent)
            }
            PUBLISHED_POST -> {
                currentPostType = PUBLISHED_POST
                loadPublishedPost(intent)
            }
            else -> {
                currentPostType = "-1"
                Log.e("StoryBuilderActivity", "loadPost [Wrong postType = ${intent.getStringExtra(POST_TYPE)}]")
            }
        }
    }

    private fun loadPrivatePost(intent: Intent) {
        val postId = intent.getStringExtra(PRIVATE_POST_ID)
        CoroutineScope(IO).launch {
            if (!postId.isNullOrEmpty()) {
                val postAndTags = storyBuilderViewModel.getPrivatePostAndNonChosenTags(postId)
                val post = postAndTags.first
                val chosenTags = post.listOfPostTags
                val nonChosenTags = postAndTags.second

                withContext(Main) {
                    listOfChosenTags = chosenTags.toMutableSet()
                    listOfNonChosenTags = nonChosenTags?.toMutableSet() ?: mutableSetOf()
                    setPostFields(post.title, post.body)
                    setTags()
                }
            }
        }
    }
    private fun loadOnModerationPost(intent: Intent) {
        val postId = intent.getStringExtra(ON_MODERATION_POST_ID)
        CoroutineScope(IO).launch {
            if (!postId.isNullOrEmpty()) {
                val postAndTags = storyBuilderViewModel.getOnModerationPostAndNonChosenTags(postId)
                val post = postAndTags.first
                val chosenTags = post?.listOfTags
                val nonChosenTags = postAndTags.second

                withContext(Main) {
                    listOfChosenTags = chosenTags?.toMutableSet() ?: mutableSetOf()
                    listOfNonChosenTags = nonChosenTags?.toMutableSet() ?: mutableSetOf()
                    setPostFields(post?.title, post?.body)
                    setTags()
                }
            }
        }
    }
    private fun loadPublishedPost(intent: Intent) {
        // TODO
    }

    private fun setTags(){
        loadTags(listOfChosenTags.map { it.name }, listOfNonChosenTags.map { it.name })
    }

    //  Перенести в Utils
    private fun createDialogSaveNote(title: String, positive: String, negative: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        builder.setPositiveButton(positive) { dialog, which ->
            savePost()
        }

        builder.setNegativeButton(negative) { dialog, which ->
            this.onBackPressed(true)
        }

        builder.show()
    }

    // Attempt to close screen
    override fun onBackPressed() {
        onBackPressed(false)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed(true)
        return true
    }
    private fun onBackPressed(shouldClose: Boolean = false) {
        if (shouldClose) {
            super.onBackPressed()
        } else {
            createDialogSaveNote("Сохранить историю?", "Сохранить на устройстве", "Удалить")
        }
    }

    // Option menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.extras == null){
            menuInflater.inflate(R.menu.menu_add_post, menu)
        }else{
            menuInflater.inflate(R.menu.menu_accept, menu)
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (intent.extras == null){
            when(item.itemId){
                R.id.addStory_menu_share -> {
                    sharePost()
                }
                R.id.addStory_menu_saveLocally -> {
                    savePost()
                }
            }
        } else{
            when(item.itemId){
                R.id.addStory_menu_acceptChanges -> {
                    saveEditedPost()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //  Load tags to container and set onClick listeners
    private fun loadTags(chosenTags: List<String>, nonChosenTags: List<String>) {
        addStory_chosenTagsContainer.tags = chosenTags.toList()
        addStory_nonChosenTagsContainer.tags = nonChosenTags.toList()
        onTagsClickListener()
    }
    private fun onTagsClickListener() {
        addStory_nonChosenTagsContainer.setOnTagClickListener(object : TagView.OnTagClickListener {
            override fun onTagClick(position: Int, text: String?) {
                onNonSelectedTagClicked(position)
            }
            override fun onTagLongClick(position: Int, text: String?) {
            }
            override fun onSelectedTagDrag(position: Int, text: String?) {}
            override fun onTagCrossClick(position: Int) {
                onTagClick(position, null)
            }
        })
        addStory_chosenTagsContainer.setOnTagClickListener(object : TagView.OnTagClickListener {
            override fun onTagClick(position: Int, text: String?) {
                onSelectedTagClicked(position)
            }
            override fun onTagLongClick(position: Int, text: String?) {}
            override fun onSelectedTagDrag(position: Int, text: String?) {}
            override fun onTagCrossClick(position: Int) {
                onTagClick(position, null)
            }
        })
    }

    // Load post fields
    private fun setPostFields(title: String?, body: String?){
        addStory_title.setText(title)
        addStory_body.setText(body)
    }

    // On selected / unselected tags clicked
    fun onSelectedTagClicked(position: Int) {
        val chosenTag = listOfChosenTags.toList()[position]
        listOfNonChosenTags.add(chosenTag)
        listOfChosenTags.remove(chosenTag)
        setTags()
    }
    fun onNonSelectedTagClicked(position: Int) {
        val chosenTag = listOfNonChosenTags.toList()[position]
        listOfChosenTags.add(chosenTag)
        listOfNonChosenTags.remove(chosenTag)
        setTags()
    }

    // Save post locally
    private fun savePost() {
        when(currentPostType){
            null -> {
                CoroutineScope(IO).launch {
                    storyBuilderViewModel.addNewPrivatePost(buildPrivatePost())
                }
            }
            PRIVATE_POST -> {
                CoroutineScope(IO).launch {
                    storyBuilderViewModel.editPrivatePost(buildPrivatePost())
                }
            }
            ON_MODERATION_POST -> {
                CoroutineScope(IO).launch {
                    storyBuilderViewModel.editOnModerationPost(buildOnModerationPost())
                }
            }
            PUBLISHED_POST -> {}
            "-1" -> {
                Log.e("StoryBuilderActivity", "savePost [Can't define post type]")
            }
        }

        onBackPressed(true)
        finish()
    }

    // Share post
    private fun sharePost() {
        CoroutineScope(IO).launch {
            storyBuilderViewModel.sharePost(buildOnModerationPost())
            withContext(Main){
                onBackPressed(true)
                finish()
            }
        }
    }

    // Get data from view and return LocalPost class
    private fun buildPrivatePost() : LocalPost{
        val title = addStory_title.text.toString()
        val body = addStory_body.text.toString()
        val currentTime = utils.getUnixTime()
        val post = LocalPost("", currentTime, title, body)
        if (currentPostType == PRIVATE_POST) {
            // Set private post id to replace it
            val postId = intent.getStringExtra(PRIVATE_POST_ID)
            if (postId != null) post.postId = postId.toLong()
        }
        post.listOfPostTags = listOfChosenTags.toMutableList()
        return post
    }
    private fun buildOnModerationPost() : RemotePost{
        val title = addStory_title.text.toString()
        val body = addStory_body.text.toString()
        val currentTime = utils.getUnixTime()
        val post = RemotePost("", currentTime, "","", null, null, title, body)
        post.listOfTags = listOfChosenTags.toList()
        if (currentPostType == ON_MODERATION_POST) {
            val postId = intent.getStringExtra(ON_MODERATION_POST_ID)
            if (postId != null) post.postId = postId
        }

        return post
    }



    private fun saveEditedPost(){
        savePost()
    }

    // Show user register screen if is not logged
    private fun startAttemptToRegisterActivity(){
        val intent = Intent(this, AttemptToRegister::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intent)
    }


}