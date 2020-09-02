package com.samriddha.runningapphilt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostId.findNavController())

        //If main activity gets destroyed when the service is running then it will trigger onCreate.
        navigateToTrackingFragment(intent)

        //For showing bottom nav only for settings,run & statistics fragment.
        navHostId.findNavController()
            .addOnDestinationChangedListener { _ , destination, _ ->

                when(destination.id){

                    R.id.settingsFragment,R.id.runFragment,R.id.statisticsFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }

        bottomNavigationView.setOnNavigationItemReselectedListener {
            /*Leave it empty we don't want to do anything if user clicks on the same fragment icon twice.*/
        }

    }

    //If main activity is not destroyed when the service is running then this fun will be triggered.
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment(intent: Intent?){
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHostId.findNavController().navigate(R.id.action_global_tracking_fragment)
        }
    }

}