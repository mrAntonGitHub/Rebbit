package com.larin_anton.rebbit.ui.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.larin_anton.rebbit.Application
import com.larin_anton.rebbit.MainActivity
import com.larin_anton.rebbit.R
import com.larin_anton.rebbit.adapters.ViewPagerAdapter
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.home.storyBuilder.StoryBuilderActivity
import com.larin_anton.rebbit.ui.home.allStories.AllStoriesFragment
import com.larin_anton.rebbit.ui.home.myStories.MyStoriesFragment
import com.larin_anton.rebbit.ui.registration.AttemptToRegister
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


private const val INIT_PREFERENCES = "INIT_PREFERENCES"

@Singleton
class HomeFragment : Fragment() {

    @Inject
    lateinit var homeViewModel: HomeViewModel

    @Inject
    lateinit var repository: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Application.application.appComponent.inject(this)
//        requireActivity().getColor(R.color.main_globalBackgroundColor).let { requireActivity().window?.setStatusBarColor(
//            it
//        )}
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CoroutineScope(IO).launch {
            setTabs()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.homeFragment_toolbar) as Toolbar
        (activity as MainActivity?)!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        homeFragment_addNewStory.setOnClickListener {
            addStory()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.homeFragment_menu_settings -> {

            }
            R.id.homeFragment_menu_nightTheme -> {

            }
            R.id.homeFragment_menu_search -> {

            }
            R.id.homeFragment_menu_signOut -> {
                val auth = FirebaseAuth.getInstance()
                auth.signOut()
                val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(
                    INIT_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                val user: SharedPreferences.Editor = sharedPreferences.edit()
                user.remove("USER_ID")
                user.apply()
                startActivity(Intent(requireActivity(), AttemptToRegister::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun addStory() {
        startActivity(Intent(this.context, StoryBuilderActivity::class.java))
    }

    private suspend fun setTabs(){
        val user = repository.getCurrentUser()
        Log.e("APPDEBUGGER2", "$user")
        withContext(Main){
            val listOfFragments = listOf(
                com.larin_anton.rebbit.models.Fragment(AllStoriesFragment(), homeViewModel.firstTagName,null),
                com.larin_anton.rebbit.models.Fragment(MyStoriesFragment(), homeViewModel.secondTagName, ResourcesCompat.getDrawable(this@HomeFragment.resources, R.drawable.ic_expand_more, null)))

            val adapter = ViewPagerAdapter(listOfFragments, childFragmentManager)
            homeFragment_viewPager.adapter = adapter
            homeFragment_tabLayout.setupWithViewPager(homeFragment_viewPager)

            setIcon()

            homeFragment_tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(p0: TabLayout.Tab?) {
                    if (p0?.position == 1) {
                        homeFragment_tabLayout.getTabAt(1)?.icon?.setTint(resources.getColor(R.color.main_primaryColor))
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    homeFragment_tabLayout.getTabAt(1)?.icon?.setTint(resources.getColor(android.R.color.darker_gray))
                    setIcon()
                }

                override fun onTabReselected(p0: TabLayout.Tab?) {}
            })


            val tabStrip = homeFragment_tabLayout.getChildAt(0) as LinearLayout
            tabStrip.getChildAt(1).setOnClickListener {
                it.showPopMenu()
            }
        }
    }

    private fun setIcon(){
        homeFragment_tabLayout.getTabAt(1)?.icon =  ResourcesCompat.getDrawable(
            this.resources,
            R.drawable.ic_expand_more,
            null
        )
        homeFragment_tabLayout.getTabAt(1)?.icon?.setTint(resources.getColor(android.R.color.darker_gray))
    }

    private fun View.showPopMenu(){
        val popupMenu = PopupMenu(requireContext(), this)
        popupMenu.menuInflater.inflate(R.menu.menu_my_stories, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem?.itemId) {
                R.id.menu_myStories_publishedPosts -> {
                    homeViewModel.secondTagName = "Опубликованные"
                    homeViewModel.changeStoryType(R.id.menu_myStories_publishedPosts)
                    homeFragment_tabLayout.getTabAt(1)?.text = homeViewModel.secondTagName
                    setColoredIcon()
                }
                R.id.menu_myStories_onModerationPosts -> {
                    homeViewModel.secondTagName = "На модерации"
                    homeViewModel.changeStoryType(R.id.menu_myStories_onModerationPosts)
                    homeFragment_tabLayout.getTabAt(1)?.text = homeViewModel.secondTagName
                    setColoredIcon()
                }
                R.id.menu_myStories_allStories -> {
                    homeViewModel.secondTagName = "Все истории"
                    homeViewModel.changeStoryType(R.id.menu_myStories_allStories)
                    homeFragment_tabLayout.getTabAt(1)?.text = homeViewModel.secondTagName
                    setColoredIcon()
                }
                R.id.menu_myStories_privatePosts -> {
                    homeViewModel.secondTagName = "Не опубликованные"
                    homeViewModel.changeStoryType(R.id.menu_myStories_privatePosts)
                    homeFragment_tabLayout.getTabAt(1)?.text = homeViewModel.secondTagName
                    setColoredIcon()
                }
            }
            false
        }
        popupMenu.show()
    }

    private fun setColoredIcon(){
        homeFragment_tabLayout.getTabAt(1)?.icon =  ResourcesCompat.getDrawable(
            this.resources,
            R.drawable.ic_expand_more,
            null
        )
        homeFragment_tabLayout.getTabAt(1)?.icon?.setTint(resources.getColor(R.color.main_primaryColor))
    }
}