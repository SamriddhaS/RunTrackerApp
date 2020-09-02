package com.samriddha.runningapphilt.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.adaptar.RunAdapter
import com.samriddha.runningapphilt.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.samriddha.runningapphilt.other.SortType
import com.samriddha.runningapphilt.other.TrackingUtility
import com.samriddha.runningapphilt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment :Fragment(R.layout.fragment_run),EasyPermissions.PermissionCallbacks {

    private val viewModel:MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
        setUpRecyclerView()

        when(viewModel.sortType){
            /*Setting the spinner selection to whatever sorting option we have at that moment.*/
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runAdapter.submitList(it)
        })


        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(adapterView: AdapterView<*>?,view: View?,position: Int,id: Long) {
                when(position){
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }/*Sorting the item's if user select's different item from spinner options*/
            }
        }

    }

    private fun setUpRecyclerView() = rvRuns.apply {

        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true)
    }

    private fun requestPermissions(){

        //Requesting For location permissions as we need them.
        if(TrackingUtility.hasLocationPermission(requireContext())){
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "Please Allow Above Permissions",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }else{
            //For android Q and above we also need to  have ACCESS_BACKGROUND_LOCATION permission as well
            EasyPermissions.requestPermissions(
                this,
                "Please Allow Above Permissions",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            //If the user has denied the permissions permanently then this dialog will be shown and it will lead to the settings for changing permissions.
            AppSettingsDialog.Builder(this).build().show()
        } else{
            //If the permissions are not denied permanently then we will request the permissions again.
            requestPermissions()
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //not needed here
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        /* "onRequestPermissionResult" is the default function that is called when we request any permission.We are using EasyPermission library
        * here that.So we are passing the flow of permission's by overriding this function and passing its parameters to
        * EasyPermissions.onRequestPermissionsResult(_,_,_,_) */
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

}