package com.larin_anton.rebbit.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.larin_anton.rebbit.data.model.*
import com.larin_anton.rebbit.data.model.notifications.Notification
import com.larin_anton.rebbit.data.model.notifications.NotificationLike
import com.larin_anton.rebbit.data.model.notifications.NotificationType
import com.larin_anton.rebbit.interfaces.RemoteRepository
import com.larin_anton.rebbit.utils.LiveDataWrapper
import com.larin_anton.rebbit.utils.Settings
import com.larin_anton.rebbit.utils.Utils
import com.google.firebase.database.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

// Posts
const val POST_NODE = "posts"
const val POST_PUBLISHED = "checked"
const val POST_ON_MODERATION = "unchecked"
const val POST_COMPLAINED_ABOUT = "complained"
const val POST_LIST_OF_TAGS = "listOfTags"
const val POST_AUTHOR_ID = "authorId"
const val POST_COUNT_OF_LIKES = "countOfLikes"
const val POST_LIST_OF_COMMENTS = "listOfComments"
const val POST_LIST_OF_LIKED_USERS_ID = "listOfLikedUsersId"
const val POST_LIST_OF_COMMENTS_USER_ID = "listOfLikedUsersId"

// Users
const val USER_NODE = "users"
const val USER_PLAIN = "plain"
const val USER_ADMIN = "admin"
const val USER_CHIEF_ADMIN = "chiefAdmin"

// User
const val USER_PRIVATE_TAGS = "listOfPrivateTags"
const val USER_LIST_OF_PUBLISHED_POSTS = "listOfPublishedPosts"
const val USER_LIST_OF_ON_MODERATION_POSTS = "listOfOnModerationPosts"
const val USER_LIST_OF_PRIVATE_POSTS = "listOfPersonalPosts"
const val USER_LIST_OF_LIKED_POSTS = "listOfLikesPostId"
const val USER_LIST_OF_BOOKMARKS_POSTS = "listOfBookmarks"
const val PUBLISHED_TIME = "published_time"
// User Notifications
const val USER_NOTIFICATIONS = "listOfNotifications"
const val USER_NOTIFICATIONS_LIKE = "likes"

// Tags
const val TAGS = "tags"
const val TAGS_PUBLISHED = "checked"
const val TAGS_ON_MODERATION = "unchecked"

// Post status
const val STATUS = "status"
const val STATUS_ON_MODERATION = "На модерации"
const val STATUS_PUBLISHED = "Опубликовано"
const val STATUS_DENIED = "Не прошло модерацию"
const val STATUS_DELETED = "Удалено"

@Singleton
class RemoteRepository @Inject constructor(val settings: Settings, val utils: Utils, val firebaseHelper: FirebaseHelper) : RemoteRepository {

    private val mutex = Mutex()

    private var firebaseDatabase = firebaseHelper.getDatabaseInstance()

    /**
     * Firebase routes
     */
    //  Posts
    private var firebasePosts = firebaseDatabase.getReference(POST_NODE)
    private val firebasePublishedPosts = firebasePosts.child(POST_PUBLISHED)
    private var firebaseOnModerationPosts = firebasePosts.child(POST_ON_MODERATION)
    private var firebaseComplainedPosts = firebasePosts.child(POST_COMPLAINED_ABOUT)

    //  Users
    private var firebaseUsers = firebaseDatabase.getReference(USER_NODE)
    private var firebasePlainUsers = firebaseUsers.child(USER_PLAIN)
    private var firebaseAdminUsers = firebaseUsers.child(USER_ADMIN)
    private var firebaseChiefAdminUsers = firebaseUsers.child(USER_CHIEF_ADMIN)

    //  Tags
    private var firebaseTags = firebaseDatabase.getReference(TAGS)

    init {
        addPublishedPostsListener()
        CoroutineScope(IO).launch {
            val currentUser = getCurrentUser()
            if (currentUser?.status != UserStatus.Undefined || currentUser.status != UserStatus.Plain) {
                addOnModerationPostsListener()
                addComplaintPostsListener()
            }
        }
    }

    /**
     * Live data
     */
    // Published Posts
    private val _listOfPublishedPosts = MutableLiveData<LiveDataWrapper<List<RemotePost>>>()
    val listOfPublishedPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfPublishedPosts
    // On moderation Posts
    private val _listOfOnModerationPosts = MutableLiveData<LiveDataWrapper<List<RemotePost>>>()
    val listOfOnModerationPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfOnModerationPosts
    // Posts that were complained about
    private val _listOfComplainPosts = MutableLiveData<LiveDataWrapper<List<RemotePost>>>()
    val listOfComplainPosts: LiveData<LiveDataWrapper<List<RemotePost>>> = _listOfComplainPosts
    // User's Notifications
    private val _notifications = MutableLiveData<LiveDataWrapper<List<Notification>>>()
    val notifications : LiveData<LiveDataWrapper<List<Notification>>> = _notifications


    /**
     * User instance and main node
     */
    private var currentUser: User? = null
    private var currentUserNode: DatabaseReference? = null


    /**
     * General methods
     */
    override suspend fun getListOfTags(): Set<Tag>? {
        //  Return List published tags or null if no tags or error
        return CompletableDeferred<Set<Tag>?>().apply {
            val deferred = this
            val listOfPublishedTags = mutableListOf<Tag>()

            firebaseTags.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { snapshot ->
                        if (!snapshot.key.isNullOrEmpty()) {
                            val tag = snapshot.getValue(String::class.java)
                            if (tag != null) {
                                listOfPublishedTags.add(Tag(tag))
                            }
                        } else {
                            Log.d("AppDebug", "RemoteRepository:onDataChange [No published tags found]")
                            deferred.complete(null)
                        }
                    }
                    deferred.complete(listOfPublishedTags.toSet())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("AppDebug", "RemoteRepository:onCancelled [Getting list of published tags cancelled]")
                    deferred.complete(null)
                }

            })
        }.await()
    }
    override suspend fun getAllUsers(): List<User>? {
        return CompletableDeferred<List<User>?>().apply {
            CoroutineScope(IO).launch {
                val plainUsers = async { getPlainUsers() }
                val adminUser = async { getAdminUsers() }
                val chiefAdminUsers = async { getChiefAdminUsers() }

                val plain = plainUsers.await() ?: emptyList()
                val admin = adminUser.await() ?: emptyList()
                val chiefAdmin = chiefAdminUsers.await() ?: emptyList()

                complete(plain + admin + chiefAdmin)
            }
        }.await()
    }
    override fun addNewUser(user: User) {
        user.status = UserStatus.Plain
        val userId = firebaseHelper.getCurrentUserId()
        if (userId != null){
            user.id = userId
            user.name = "user${utils.getUnixTime()}"
            user.rating = "0"
            firebasePlainUsers.child(userId).setValue(user)
        } else {
            Log.d("RemoteRepository", "addNewUser [User id is empty! Can't add this user!]")
        }
    }
    override suspend fun increaseUser(userId: String): Boolean {
        val users = getAllUsers()
        val userToIncrease = users?.firstOrNull{
            it.id == userId
        }
        val userToIncreaseStatus = userToIncrease?.status
        val currentUser = getCurrentUser()

        return if (currentUser != null && userToIncreaseStatus != null) {
            val necessaryLevel = when(userToIncreaseStatus){
                UserStatus.Plain -> {
                    UserStatus.Admin
                }
                UserStatus.Admin -> {
                    UserStatus.ChiefAdmin
                }
                else -> {
                    null
                }
            }
            return if (necessaryLevel != null){
                return if (isActionAllowed(currentUser, necessaryLevel)) {
                    val userToIncreaseId = userToIncrease.id
                    val userToIncreaseNode = getUserNode(userToIncreaseId)
                    return if (userToIncreaseNode != null) {
                        if (userToIncreaseStatus == UserStatus.Plain) {
                            moveFirebaseRecord(userToIncreaseNode, firebaseAdminUsers.child(userToIncreaseId))
                            true
                        }
                        else if(userToIncreaseStatus == UserStatus.Admin){
                            moveFirebaseRecord(userToIncreaseNode, firebaseChiefAdminUsers.child(userToIncreaseId))
                            true
                        }
                        else{
                            false
                        }
                    } else {
                        Log.d("RemoteRepository", "increaseUser [Can't get user node to increase]")
                        false
                    }
                } else {
                    Log.d("RemoteRepository", "increaseUser [This user can't do that]")
                    false
                }
            }else{
                Log.d("RemoteRepository", "increaseUser [This user can't be increased]")
                false
            }
        } else {
            Log.d("RemoteRepository", "increaseUser [Can't get current user = $currentUser or userToIncreaseStatus = $userToIncreaseStatus]")
            false
        }
    }
    override suspend fun decreaseUser(userId: String): Boolean {
        val users = getAllUsers()
        val userToDecrease = users?.firstOrNull{
            it.id == userId
        }
        val userToDecreaseStatus = userToDecrease?.status
        val currentUser = getCurrentUser()

        return if (currentUser != null && userToDecreaseStatus != null) {
            val necessaryLevel = when(userToDecreaseStatus){
                UserStatus.Admin -> {
                    UserStatus.Admin
                }
                UserStatus.ChiefAdmin -> {
                    UserStatus.ChiefAdmin
                }
                else -> {
                    null
                }
            }
            return if (necessaryLevel != null){
                return if (isActionAllowed(currentUser, necessaryLevel)) {
                    val userToDecreaseId = userToDecrease.id
                    val userToDecreaseNode = getUserNode(userToDecreaseId)
                    return if (userToDecreaseNode != null) {
                        if (userToDecreaseStatus == UserStatus.Admin) {
                            moveFirebaseRecord(userToDecreaseNode, firebasePlainUsers.child(userToDecreaseId))
                            true
                        }
                        else if(userToDecreaseStatus == UserStatus.ChiefAdmin){
                            moveFirebaseRecord(userToDecreaseNode, firebaseAdminUsers.child(userToDecreaseId))
                            true
                        }
                        else{
                            false
                        }
                    } else {
                        Log.d("RemoteRepository", "decreaseUser [Can't get user node to decrease]")
                        false
                    }
                } else {
                    Log.d("RemoteRepository", "decreaseUser [This user can't do that]")
                    false
                }
            }else{
                Log.d("RemoteRepository", "decreaseUser [This user can't be decreased]")
                false
            }
        } else {
            Log.d("RemoteRepository", "decreaseUser [Can't get current user = $currentUser or userToIncreaseStatus = $userToDecreaseStatus]")
            false
        }
    }
    private suspend fun getPlainUsers(): List<User>? {
        return firebasePlainUsers.getListOfUsers()
    }
    private suspend fun getAdminUsers(): List<User>? {
        return firebaseAdminUsers.getListOfUsers()
    }
    private suspend fun getChiefAdminUsers(): List<User>? {
        return firebaseChiefAdminUsers.getListOfUsers()
    }
    private suspend fun DatabaseReference.getListOfUsers() : List<User>?{
        // Return list of users in DatabaseReference path
        return CompletableDeferred<List<User>?>().apply {
            val deferred = this
            this@getListOfUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<User>()
                    snapshot.children.forEach {
                        val user: User? = it.getValue(User::class.java)
                        when(this@getListOfUsers){
                            firebasePlainUsers -> {
                                user?.status = UserStatus.Plain
                            }
                            firebaseAdminUsers -> {
                                user?.status = UserStatus.Admin
                            }
                            firebaseChiefAdminUsers ->{
                                user?.status = UserStatus.ChiefAdmin
                            }
                        }
                        if (user != null) users.add(user)
                    }
                    deferred.complete(users)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("RemoteRepository", "onCancelled [Can't get users: $error]")
                    deferred.complete(null)
                }
            })
        }.await()
    }
    private suspend fun getUser(userId: String) : User?{
        val users = getAllUsers()
        return users?.firstOrNull{
            it.id == userId
        }
    }


    /**
     * Current user methods
     */
    override suspend fun getCurrentUser(): User? {
        //  Return null if user is not exist
        return if (currentUser == null) {
            if (setCurrentUser()) {
                currentUser
            } else {
                null
            }
        } else {
            currentUser
        }
    }
    override suspend fun setCurrentUser(): Boolean {
        //  Return false if user doesn't exist
        return CompletableDeferred<Boolean>().apply {
            mutex.withLock {
                if (currentUser == null) {
                    if (setUser()){
                        complete(true)
                    }else{
                        Log.d("RemoteRepository", "setCurrentUser [User is not exist]")
                        complete(false)
                    }
                } else {
                    Log.d("AppDebug", "RemoteRepository:setCurrentUser [User was already set]")
                    this.complete(true)
                }
            }
        }.await()
    }
    override suspend fun removeCurrentUserAccount(): Boolean {
        val currentUser = getCurrentUser()
        val currentUserNode = getCurrentUserNode()
        return if (currentUser != null && currentUserNode != null) {
            currentUserNode.removeValue()
            firebaseHelper.removeCurrentUser()
            true
        } else {
            Log.d("RemoteRepository", "removeCurrentUserAccount [Can't get current user]")
            false
        }

    }
    override suspend fun getListOfUserPublishedPosts(): List<RemotePost>? {
        val userNode = getCurrentUserNode()
        return CompletableDeferred<List<RemotePost>?>().apply {
            val deferred = this
            if (userNode != null) {
                userNode.child(USER_LIST_OF_PUBLISHED_POSTS).orderByChild(PUBLISHED_TIME).addListenerForSingleValueEvent(object : ValueEventListener {
                    val listOfPublishedPosts = mutableListOf<RemotePost>()
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach { snapshot ->
                            if (!snapshot.key.isNullOrEmpty()) {
                                val localPost = snapshot.getValue(RemotePost::class.java)
                                localPost?.let { listOfPublishedPosts.add(it) }
                            }
                        }
                        deferred.complete(listOfPublishedPosts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("AppDebug", "RemoteRepository:onCancelled [Can't add user published post listener]")
                        deferred.complete(null)
                    }
                })
            } else {
                deferred.complete(null)
                Log.e("AppDebug", "RemoteRepository:addUserPrivatePostsListener [Can't get user node. It's empty]")
            }
        }.await()
    }
    override suspend fun getListOfUserOnModerationPosts(): List<RemotePost>? {
        val userNode = getCurrentUserNode()
        return CompletableDeferred<List<RemotePost>?>().apply {
            val deferred = this
            if (userNode != null) {
                userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).orderByChild(PUBLISHED_TIME).addListenerForSingleValueEvent(object : ValueEventListener {
                    val listOfPublishedPosts = mutableListOf<RemotePost>()
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach { snapshot ->
                            if (!snapshot.key.isNullOrEmpty()) {
                                val localPost = snapshot.getValue(RemotePost::class.java)
                                localPost?.let { listOfPublishedPosts.add(it) }
                            }
                        }
                        deferred.complete(listOfPublishedPosts)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("RemoteRepository", "onCancelled [Can't get user's on moderation posts listener]")
                        deferred.complete(null)
                    }
                })
            } else {
                deferred.complete(null)
                Log.e("RemoteRepository", "getUserOnModerationPosts [Can't get user node. It's empty]")
            }
        }.await()
    }
    override suspend fun getListOfUserPrivatePosts() : List<LocalPost>?{
        val userNode = getCurrentUserNode()
        return CompletableDeferred<List<LocalPost>?>().apply {
            val deferred = this
            if (userNode != null) {
                userNode.child(USER_LIST_OF_PRIVATE_POSTS).orderByChild(PUBLISHED_TIME).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val listOfUserPrivatePosts : MutableSet<LocalPost> = mutableSetOf()
                        dataSnapshot.children.forEach { snapshot ->
                            if (!snapshot.key.isNullOrEmpty()) {
                                val localPost = snapshot.getValue(LocalPost::class.java)
                                localPost?.let { listOfUserPrivatePosts.add(it) }
                            }
                        }
                        deferred.complete(listOfUserPrivatePosts.toList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("AppDebug", "RemoteRepository:onCancelled [Can't add user private post listener]")
                        deferred.complete(null)
                    }
                })
            } else {
                Log.e("AppDebug", "RemoteRepository:addUserPrivatePostsListener [Can't get user node. It's empty]")
                deferred.complete(null)
            }
        }.await()
    }


    override fun signOutCurrentUser(){
        firebaseHelper.signOut()
    }
    private suspend fun setUser(): Boolean {
        val users = getAllUsers()
        val currentUserId = firebaseHelper.getCurrentUserId()
        val user = users?.firstOrNull {
            it.id == currentUserId
        }
        Log.e("1231qwe", "${user}")
        if (user != null){
            currentUser = user
            currentUserNode = getUserNode(user.id)
            observeCurrentUserNotifications()
        }

        return user != null
    }
    private suspend fun getCurrentUserNode(): DatabaseReference? {
        //  Return null if userNode is not exist
        return if (currentUser == null) {
            if (setCurrentUser()) {
                currentUserNode
            } else {
                null
            }
        } else {
            currentUserNode
        }
    }
    private suspend fun getUserNode(userId: String): DatabaseReference? {
        val user: User? = getUser(userId)
        val userStatus = user?.status
        if (userStatus != null) {
            return when (userStatus) {
                UserStatus.Plain -> {
                    firebasePlainUsers.child(userId)
                }
                UserStatus.Admin -> {
                    firebaseAdminUsers.child(userId)
                }
                UserStatus.ChiefAdmin -> {
                    firebaseChiefAdminUsers.child(userId)
                }
                else -> {
                    null
                }
            }
        } else {
            return null
        }
    }

    /**
     * Notifications
     */
    private suspend fun addOnLikeSetNotification(postId: String, user: User){
        val postAuthor = getPost(postId)?.authorId
        val post = getPost(postId)
        if (postAuthor != null && post != null) {
            val key = getUserNode(postAuthor)?.child(USER_NOTIFICATIONS)?.child(USER_NOTIFICATIONS_LIKE)?.push()?.key
            val notification = NotificationLike(postId, post, key.toString(), user.id, user.name, user.image, utils.getUnixTime())
            getUserNode(postAuthor)?.child(USER_NOTIFICATIONS)?.child(USER_NOTIFICATIONS_LIKE)?.child(key.toString())?.setValue(notification)
        }
    }
    private suspend fun removeOnLikeSetNotification(postId: String, user: User){
        val postAuthor = getPost(postId)?.authorId
        if (postAuthor != null) {
            getUserNode(postAuthor)?.child(USER_NOTIFICATIONS)?.child(USER_NOTIFICATIONS_LIKE)?.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likeNotification : DataSnapshot? = snapshot.children.find {
                        ((it.getValue(NotificationLike::class.java))?.postId == postId)
                    }
                    removeLikeNotification(postAuthor, likeNotification?.key)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
    private fun removeLikeNotification(postAuthor : String, key: String?){
        CoroutineScope(IO).launch {
            if (key != null){
                getUserNode(postAuthor)?.child(USER_NOTIFICATIONS)?.child(USER_NOTIFICATIONS_LIKE)?.child(key.toString())?.removeValue()
            }else{
                Log.e("RemoteRepository", "removeOnLikeSetNotification [Key is null]")
            }
        }
    }
    suspend fun removeLikeNotification(likeNotificationId: String) : Boolean{
        return CompletableDeferred<Boolean>().apply {
            getCurrentUserNode()?.child(USER_NOTIFICATIONS)?.child(USER_NOTIFICATIONS_LIKE)?.child(likeNotificationId)?.removeValue()?.addOnCompleteListener {
                if (it.isSuccessful){
                    this.complete(true)
                }else{
                    this.complete(false)
                }
            }
        }.await()
    }

    /**
     * Posts change listeners
     */
    // Update LiveData on change
    private fun addPublishedPostsListener() {
        firebasePublishedPosts.orderByChild("published_time")
            .addValueEventListener(object : ValueEventListener {
                val listOfPublishedPosts = mutableListOf<RemotePost>()
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    _listOfPublishedPosts.value = LiveDataWrapper.loading()
                    listOfPublishedPosts.clear()
                    dataSnapshot.children.forEach { snapshot ->
                        if (!snapshot.key.isNullOrEmpty()) {
                            val remotePost = snapshot.getValue(RemotePost::class.java)
                            remotePost?.postId = snapshot.key.toString()
                            remotePost?.let { listOfPublishedPosts.add(it) }
                        }
                    }
                    _listOfPublishedPosts.value = LiveDataWrapper.success(listOfPublishedPosts)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    _listOfPublishedPosts.value = LiveDataWrapper.error(databaseError.toString())
                }
            })
    }
    private fun addOnModerationPostsListener() {
        firebaseOnModerationPosts.addValueEventListener(object : ValueEventListener {

            val listOfUncheckedPosts = mutableListOf<RemotePost>()

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _listOfOnModerationPosts.value = LiveDataWrapper.loading()
                listOfUncheckedPosts.clear()
                dataSnapshot.children.forEach { snapshot ->
                    if (!snapshot.key.isNullOrEmpty()) {
                        val remotePost = snapshot.getValue(RemotePost::class.java)
                        remotePost?.postId = snapshot.key.toString()
                        remotePost?.let { listOfUncheckedPosts.add(it) }
                    }
                }
                _listOfOnModerationPosts.value = LiveDataWrapper.success(listOfUncheckedPosts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AppDebug", "RemoteRepository:onCancelled [${databaseError}]")
                _listOfOnModerationPosts.value = LiveDataWrapper.error(databaseError.toString())
            }
        })
    }
    private fun addComplaintPostsListener() {
        firebaseComplainedPosts.addValueEventListener(object : ValueEventListener {

            val listOfComplainPosts = mutableListOf<RemotePost>()

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _listOfComplainPosts.value = LiveDataWrapper.loading()
                listOfComplainPosts.clear()
                dataSnapshot.children.forEach { snapshot ->
                    if (!snapshot.key.isNullOrEmpty()) {
                        val remotePost = snapshot.getValue(RemotePost::class.java)
                        remotePost?.postId = snapshot.key.toString()
                        remotePost?.let { listOfComplainPosts.add(it) }
                    }
                }
                _listOfComplainPosts.value = LiveDataWrapper.success(listOfComplainPosts)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AppDebug", "RemoteRepository:onCancelled [${databaseError}]")
                _listOfComplainPosts.value = LiveDataWrapper.error(databaseError.toString())
            }
        })
    }
    suspend fun observeCurrentUserNotifications(){
        getCurrentUserNode()?.child(USER_NOTIFICATIONS)?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val likeNotifications = mutableListOf<Notification>()
                snapshot.child(USER_NOTIFICATIONS_LIKE).children.forEach {
                    val likeNotification = it.getValue(NotificationLike::class.java)
                    if (likeNotification != null) {
                        val notification = Notification(NotificationType.LIKE, likeNotification)
                        likeNotifications.add(notification)
                    }
                }
                _notifications.value = LiveDataWrapper.success(likeNotifications)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    /**
     * Post
     */
    // General
    override suspend fun getPost(postId: String) : RemotePost?{
        val posts = listOfPublishedPosts.value?.data
        return posts?.firstOrNull{
            it.postId == postId
        }
    }
    override suspend fun setLikePost(postId: String): Boolean {
        val currentUser = getCurrentUser()
        return if (currentUser == null) {
            false
        } else {
            return if (setOrRemoveLikePost(postId, true)){
                addOnLikeSetNotification(postId, currentUser)
                true
            }else{
                false
            }
        }
    }
    override suspend fun removeLikePost(postId: String): Boolean {
        val currentUser = getCurrentUser()
        return if (currentUser == null) {
            false
        } else {
            return if (setOrRemoveLikePost(postId, false)){
                removeOnLikeSetNotification(postId, currentUser)
                true
            }else{
                false
            }
        }
    }
    private suspend fun setOrRemoveLikePost(postId: String, shouldSet: Boolean): Boolean {
        // Prepare and check data for setOrRemoveLike method
        val currentUser = getCurrentUser()
        return if (currentUser != null){
            val post = getPost(postId)
            return if (post != null){
                val authorNode = getUserNode(post.authorId)
                return if (authorNode != null){
                    setOrRemoveLike(authorNode, post, shouldSet, currentUser.id, currentUser.name)
                    true
                }else false
            } else{
                false
            }
        }else false
    }
    private fun setOrRemoveLike(userMainNode: DatabaseReference, post: RemotePost, shouldSet: Boolean, currentUserId: String, currentUserName: String){
        CoroutineScope(IO).launch {
            // Set or remove likes to / from post author path, published posts path and user's which set the liked path
            val countOfLikes = post.listOfLikedUsersId?.size ?: 0
            val operation = if (shouldSet) 1 else -1

            userMainNode.child(USER_LIST_OF_PUBLISHED_POSTS).child(post.postId).child(POST_COUNT_OF_LIKES).setValue((countOfLikes + operation).toString())
            firebasePublishedPosts.child(post.postId).child(POST_COUNT_OF_LIKES).setValue((countOfLikes + operation).toString())

            if (shouldSet) {
                firebasePublishedPosts.child(post.postId).child(POST_LIST_OF_LIKED_USERS_ID).child(currentUserId).setValue(currentUserName)
                currentUserNode?.child(USER_LIST_OF_LIKED_POSTS)?.child(post.postId)?.setValue(utils.getUnixTime())
            } else {
                firebasePublishedPosts.child(post.postId).child(POST_LIST_OF_LIKED_USERS_ID).child(currentUserId).removeValue()
                currentUserNode?.child(USER_LIST_OF_LIKED_POSTS)?.child(post.postId)?.removeValue()
            }

        }
    }
    private suspend fun changeUserPostStatus(postType: PostType, postId: String, status: String) {
        val postAuthorId = getPost(postId)?.authorId
        if (postAuthorId != null){
            val userNode = getUserNode(postAuthorId)
            when(postType){
                PostType.PRIVATE -> {
                    userNode?.child(USER_LIST_OF_PRIVATE_POSTS)?.child(postId)?.child(STATUS)?.setValue(status)
                }
                PostType.MODERATION -> {
                    userNode?.child(USER_LIST_OF_ON_MODERATION_POSTS)?.child(postId)?.child(STATUS)?.setValue(status)
                }
                PostType.PUBLISHED -> {
                    userNode?.child(USER_LIST_OF_PUBLISHED_POSTS)?.child(postId)?.child(STATUS)?.setValue(status)
                }
                else -> {}
            }
        }
    }

    // On moderation posts
    override suspend fun addOnModerationPost(post: RemotePost){
        val currentFirebaseUser = getCurrentUser()
        val userNode = getCurrentUserNode()
        if (currentFirebaseUser != null && userNode != null) {
            val onModerationPost = RemotePost(
                STATUS_ON_MODERATION, post.published_time, currentFirebaseUser.id,
                currentFirebaseUser.name, null,
                null,
                post.title, post.body)
            val key = currentUserNode?.child(USER_LIST_OF_ON_MODERATION_POSTS)?.push()?.key
            onModerationPost.postId = key ?: ""
            if (key != null) {
                userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).child(key).setValue(onModerationPost)
                userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).child(key).child(POST_LIST_OF_TAGS).setValue(post.listOfTags)
                copyFirebaseRecord(userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).child(key), firebaseOnModerationPosts.child(key))
            }
        } else {
            Log.d("RemoteRepository", "addOnModerationPost [Error while adding unchecked post: firebaseUser = $currentFirebaseUser, user = ${getCurrentUser()?.id}, userNode = $userNode]")
        }
    }
    override suspend fun moveOnModerationPostToPublished(postId: String) {
        // Move on moderation post to published path
        val currentUser = getCurrentUser()
        if (currentUser != null) {
            if (isActionAllowed(currentUser, UserStatus.Admin) || isActionAllowed(currentUser, UserStatus.ChiefAdmin)){
                moveFirebaseRecord(firebaseOnModerationPosts.child(postId), firebasePublishedPosts.child(postId))

                val authorId = firebaseOnModerationPosts.child(postId).child(POST_AUTHOR_ID).toString()

                val authorOnModerationPath = getUserNode(authorId)?.child(
                    USER_LIST_OF_ON_MODERATION_POSTS
                )?.child(postId)
                val authorPublishedPath = getUserNode(authorId)?.child(USER_LIST_OF_PUBLISHED_POSTS)?.child(postId)

                if (authorOnModerationPath != null && authorPublishedPath != null){
                    moveFirebaseRecord(authorOnModerationPath, authorPublishedPath)
                    changeUserPostStatus(PostType.PUBLISHED, postId, STATUS_PUBLISHED)
                }
            }
        } else {
            Log.d("RemoteRepository", "moveOnModerationPostToPublished [Current user is null]")
        }
    }
    override suspend fun rejectOnModerationPost(postId: String) {
        val authorId = getPost(postId)?.authorId
        if (authorId != null){
            val authorNode = getUserNode(authorId)
            firebaseOnModerationPosts.child(postId).removeValue()
            authorNode?.child(USER_LIST_OF_ON_MODERATION_POSTS)?.child(postId)?.child(STATUS)?.setValue(
                STATUS_DENIED
            )
        }
    }
    override suspend fun editOnModerationPost(post: RemotePost){
        val userNode = getCurrentUserNode()
        if (userNode != null){
            post.status = STATUS_ON_MODERATION
            userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).child(post.postId).setValue(post)
            userNode.child(USER_LIST_OF_ON_MODERATION_POSTS).child(post.postId).child(POST_LIST_OF_TAGS).setValue(post.listOfTags)
            firebaseOnModerationPosts.child(post.postId).setValue(post)
        }else{
            Log.d("RemoteRepository", "editOnModerationPost [Can't get user node. It's null]")
        }
    }
    override suspend fun getOnModerationPost(postId: String) : RemotePost?{
        val currentUserOnModerationPosts = getListOfUserOnModerationPosts()
        return currentUserOnModerationPosts?.firstOrNull {
            it.postId == postId
        }
    }

    // Private posts
    override suspend fun addPrivatePost(post: LocalPost) {
        val currentUser = getCurrentUserNode()
        currentUser?.child(USER_LIST_OF_PRIVATE_POSTS)?.push()?.setValue(post)
        currentUser?.child(USER_LIST_OF_PRIVATE_POSTS)?.child(POST_LIST_OF_TAGS)?.setValue(post.listOfPostTags)
    }
    override suspend fun addPrivatePost(listOfPosts: List<LocalPost>) {
        val currentUserNode = getCurrentUserNode()
        listOfPosts.forEach { post ->
            currentUserNode?.child(USER_LIST_OF_PRIVATE_POSTS)?.push()?.setValue(post)
        }
    }
    override suspend fun removePrivatePost(postId: String) {
        val currentUser = getCurrentUserNode()
        currentUser?.child(USER_LIST_OF_PRIVATE_POSTS)?.child(postId)?.removeValue()
    }
    override suspend fun removeAllPrivatePosts() {
        val userNode = getCurrentUserNode()
        userNode?.child(USER_LIST_OF_PRIVATE_POSTS)?.removeValue()
    }

    // Published posts
    override suspend fun removePublishedPost(postId: String) {
        val currentUser = currentUser
        if (currentUser != null) {
            if (isActionAllowed(currentUser, UserStatus.Admin) || isActionAllowed(currentUser, UserStatus.ChiefAdmin)) {
                val authorId = firebasePublishedPosts.child(postId).child(POST_AUTHOR_ID).toString()
                val authorNode = getUserNode(authorId)
                if (authorNode != null) {
                    changeUserPostStatus(PostType.PUBLISHED, postId, STATUS_DELETED)
                }
                firebasePublishedPosts.child(postId).removeValue()
            }
        }
    }
    override suspend fun commentPost(postId: String, comment: Comment) : Boolean{
        return CompletableDeferred<Boolean>().apply{
            val commentId = firebasePublishedPosts.child(postId).child(POST_LIST_OF_COMMENTS).push().key
            if (commentId != null) {
                comment.commentId = commentId
                firebasePublishedPosts.child(postId).child(POST_LIST_OF_COMMENTS).child(commentId).setValue(comment).addOnCompleteListener {
                    if (it.isSuccessful){
                        this.complete(true)
                    } else{
                        this.complete(false)
                    }
                }
            }
        }.await()
    }

    suspend fun addPostToBookmarks(postId: String, isAddSet: Boolean) : Boolean{
        val post = getPost(postId)
        return if (post?.postId != null){
            return if (isAddSet){
                getCurrentUserNode()?.child(USER_LIST_OF_BOOKMARKS_POSTS)?.child(post.postId)?.setValue(post)
                true
            }else{
                getCurrentUserNode()?.child(USER_LIST_OF_BOOKMARKS_POSTS)?.child(post.postId)?.removeValue()
                true
            }
        } else{
            false
        }
    }
    suspend fun getBookmarksPosts() : List<RemotePost>?{
        return CompletableDeferred<List<RemotePost>?>().apply {
            val deferred = this
            val listOfBookedPosts = mutableListOf<RemotePost>()

            getCurrentUserNode()?.child(USER_LIST_OF_BOOKMARKS_POSTS)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { snapshot ->
                        if (!snapshot.key.isNullOrEmpty()) {
                            val post = snapshot.getValue(RemotePost::class.java)
                            if (post != null) {
                                listOfBookedPosts.add(post)
                            }
                        } else {
                            Log.d("AppDebug", "RemoteRepository:onDataChange [No published tags found]")
                            deferred.complete(null)
                        }
                    }
                    deferred.complete(listOfBookedPosts)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("AppDebug", "RemoteRepository:onCancelled [Getting list of published tags cancelled]")
                    deferred.complete(null)
                }

            })
        }.await()
    }
    suspend fun removeOwnComment(postId: String, comment: Comment) : Boolean{
        return CompletableDeferred<Boolean>().apply{
            val post = getPost(postId)
            val currentUserId = getCurrentUser()?.id
            if (comment.userId == currentUserId){
                firebasePublishedPosts.child(postId).child(POST_LIST_OF_COMMENTS).child(comment.commentId).removeValue().addOnCompleteListener {
                    if (it.isSuccessful){
                        this.complete(true)
                    }
                    else{
                        this.complete(false)
                    }
                }
            }
        }.await()
    }

    // Complain
    override suspend fun complainAboutPost(postId: String) {
        // TODO
        copyCheckedPostIdToComplainPostsResult(postId)
    }
    private fun copyCheckedPostIdToComplainPostsResult(postId: String): Boolean {
        val currentUser = currentUser
        return if (currentUser != null) {
            return when (currentUser.status) {
                UserStatus.Plain, UserStatus.Admin, UserStatus.ChiefAdmin -> {
                    firebaseComplainedPosts.setValue(postId)
                    true
                }
                else -> {
                    Log.e(
                        "AppDebug",
                        "RemoteRepository:copyCheckedPostIdToComplainPostsResult [Current user is ${currentUser.status}]"
                    )
                    false
                }
            }
        } else {
            false
        }
    }

    /**
     * Tags
     */
    override suspend fun addTag(tag: Tag){
        val tags = getListOfTags()
        val containTag : Tag? = tags?.firstOrNull{
            it.name == tag.name
        }
        if (containTag == null){
            firebaseTags.push().setValue(tag)
        }
    }


    /**
     * Support methods
     */
    private fun isActionAllowed(currentUser: User, necessaryLevel: UserStatus): Boolean {
        return when (currentUser.status) {
            UserStatus.Plain -> {
                Log.d("RemoteRepository", "isActionAllowed [Plain user can't do that]")
                false
            }
            UserStatus.Admin -> {
                when (necessaryLevel) {
                    UserStatus.Plain -> {
                        true
                    }
                    else -> {
                        Log.d("RemoteRepository", "isActionAllowed [Admin user can't do that]")
                        false
                    }
                }
            }
            UserStatus.ChiefAdmin -> {
                true
            }
            else -> {
                Log.d("RemoteRepository", "isActionAllowed [Can't get user status]")
                false
            }
        }
    }

    // CHECK THEM
    private fun moveFirebaseRecord(fromPath: DatabaseReference, toPath: DatabaseReference) {
        fromPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toPath.setValue(dataSnapshot.value) { error, _ ->
                    if (error != null) {
                        Log.e("AppDebug", "RemoteRepository:onComplete [WAS ERROR]")
                    } else {
                        fromPath.removeValue()
                        Log.e("AppDebug", "RemoteRepository:onComplete [SUCCESSFULLY DONE]")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AppDebug", "RemoteRepository:onCancelled [ERROR = $error]")
            }
        })
    }
    private fun copyFirebaseRecord(fromPath: DatabaseReference, toPath: DatabaseReference) {
        fromPath.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toPath.setValue(dataSnapshot.value, object : DatabaseReference.CompletionListener {
                    override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                        if (error != null) {
                            Log.e("AppDebug", "RemoteRepository:onComplete [WAS ERROR]")
                        } else {
                            Log.e("AppDebug", "RemoteRepository:onComplete [SUCCESSFULLY DONE]")
                        }
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AppDebug", "RemoteRepository:onCancelled [ERROR = $error]")
            }
        })
    }
}