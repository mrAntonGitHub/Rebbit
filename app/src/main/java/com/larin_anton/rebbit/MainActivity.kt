package com.larin_anton.rebbit

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.larin_anton.rebbit.data.model.LiveDataStatus
import com.larin_anton.rebbit.repository.Repository
import com.larin_anton.rebbit.ui.notifications.NotificationsFragment
import com.larin_anton.rebbit.utils.KeepStateNavigator
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var notificationFragment : NotificationsFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Application.application.appComponent.inject(this)
        setContentView(R.layout.activity_main)
        setNavigation()

        repository.notifications.observe(this, {
            if (it.status == LiveDataStatus.SUCCESS){
                if (it.data != null){
                    Log.e("MainActivity", "onCreate [${it.data}]")
                }
            }
        })
    }


    private fun setNavigation(){
        val navController = findNavController(R.id.nav_host_fragment)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)!!
        val navigator = KeepStateNavigator(this, navHostFragment.childFragmentManager, R.id.nav_host_fragment)
        navController.navigatorProvider.addNavigator(navigator)
        navController.setGraph(R.navigation.mobile_navigation)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)
    }
}