package com.larin_anton.rebbit.di.components

import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.MainActivity
import com.larin_anton.rebbit.di.modules.AppModule
import com.larin_anton.rebbit.di.modules.DatabaseModule
import com.larin_anton.rebbit.ui.bookmark.BookmarkFragment
import com.larin_anton.rebbit.ui.bookmark.BookmarkViewModel
import com.larin_anton.rebbit.ui.comments.CommentsActivity
import com.larin_anton.rebbit.ui.comments.CommentsViewModel
import com.larin_anton.rebbit.ui.dialogs.ComplainDialogFragment
import com.larin_anton.rebbit.ui.home.HomeFragment
import com.larin_anton.rebbit.ui.home.HomeViewModel
import com.larin_anton.rebbit.ui.home.storyBuilder.StoryBuilderActivity
import com.larin_anton.rebbit.ui.home.storyBuilder.StoryBuilderViewModel
import com.larin_anton.rebbit.ui.home.allStories.AllStoriesFragment
import com.larin_anton.rebbit.ui.home.allStories.AllStoriesViewModel
import com.larin_anton.rebbit.ui.home.myStories.MyStoriesFragment
import com.larin_anton.rebbit.ui.home.myStories.MyStoriesViewModel
import com.larin_anton.rebbit.ui.notifications.NotificationsFragment
import com.larin_anton.rebbit.ui.notifications.NotificationsViewModel
import com.larin_anton.rebbit.ui.profile.ProfileFragment
import com.larin_anton.rebbit.ui.profile.ProfileViewModel
import com.larin_anton.rebbit.ui.registration.AttemptToRegister
import com.larin_anton.rebbit.ui.registration.SignUpActivity
import com.larin_anton.rebbit.ui.registration.SignInActivity
import com.larin_anton.rebbit.ui.welcome.WelcomeActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, DatabaseModule::class])
interface AppComponent {
    fun inject(application: Application)
    fun inject(allStoriesViewModel: AllStoriesViewModel)
    fun inject(allStoriesFragment: AllStoriesFragment)
    fun inject(storyBuilderActivity: StoryBuilderActivity)
    fun inject(storyBuilderViewModel: StoryBuilderViewModel)
    fun inject(myStoriesFragment: MyStoriesFragment)
    fun inject(myStoriesViewModel: MyStoriesViewModel)
    fun inject(signInActivity: SignInActivity)
    fun inject(signUpActivity: SignUpActivity)
    fun inject(welcomeActivity: WelcomeActivity)
    fun inject(profileFragment: ProfileFragment)
    fun inject(mainActivity: MainActivity)
    fun inject(homeFragment: HomeFragment)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(attemptToRegister: AttemptToRegister)
    fun inject(profileViewModel: ProfileViewModel)
    fun inject(bookmarkFragment: BookmarkFragment)
    fun inject(bookmarkViewModel: BookmarkViewModel)
    fun inject(complainDialogFragment: ComplainDialogFragment)
    fun inject(commentsActivity: CommentsActivity)
    fun inject(commentsViewModel: CommentsViewModel)
    fun inject(notificationsFragment: NotificationsFragment)
    fun inject(notificationsViewModel: NotificationsViewModel)
}