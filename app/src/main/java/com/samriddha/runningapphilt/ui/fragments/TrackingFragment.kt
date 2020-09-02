package com.samriddha.runningapphilt.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.data.db.Run
import com.samriddha.runningapphilt.other.Constants.ACTION_PAUSE_SERVICE
import com.samriddha.runningapphilt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.samriddha.runningapphilt.other.Constants.ACTION_STOP_SERVICE
import com.samriddha.runningapphilt.other.Constants.CANCEL_TRACKING_DIALOG
import com.samriddha.runningapphilt.other.Constants.MAP_ZOOM
import com.samriddha.runningapphilt.other.Constants.POLYLINE_COLOR
import com.samriddha.runningapphilt.other.Constants.POLYLINE_WIDTH
import com.samriddha.runningapphilt.other.TrackingUtility
import com.samriddha.runningapphilt.services.Polyline
import com.samriddha.runningapphilt.services.Polylines
import com.samriddha.runningapphilt.services.TrackingService
import com.samriddha.runningapphilt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment :Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var map:GoogleMap? = null
    private var isTracking:Boolean = false
    private var pathPoints = mutableListOf<MutableList<LatLng>>()
    private var currentTimMillis = 0L
    private var menu: Menu? = null

    @set:Inject
    var weight = 70f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)


        if (savedInstanceState!=null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG) as CancelTrackingDialogFragment?

            cancelTrackingDialog?.setYesListener { stopRun() }
        }


        mapView.getMapAsync {
            map = it
            connectAllPolylines() //if there are already existing points inside our pathPoints var we connect them.

        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObservers()

        btnFinishRun.setOnClickListener {
            zoomOutToSeeWholeTrack()
            endRanAndSaveDb()
        }

    }

    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it) // whenever isTracking state is changed we update toggleRun button's text and finishRun button's visibility.
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            /*Observing if there is new item inside our pathPoints variable.If user is running(changing location) then new points
            * will be added inside this variable.*/
            pathPoints = it // saving updated path points inside our local pathPoints variable.
            connectLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRanInMillis.observe(viewLifecycleOwner, Observer {
            currentTimMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimMillis,true)
            tvTimer.text = formattedTime //updating tvTimer text-view whenever timeRanInMillis is updated.
        })
    }

    /*We want to either start or pause the service if user presses toggle run button*/
    private fun toggleRun(){
        if (isTracking){
            //menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    /*Updating toggleRun buttons text and finishRun buttons visibility depending on the state of isTracking variable.*/
    private fun updateTracking(isTracking:Boolean){

        this.isTracking = isTracking
        if (!isTracking && currentTimMillis>0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else if (isTracking){
            menu?.getItem(0)?.isVisible = true
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    /*Moving the map to our current location. "pathPoints.last().last()" holds our current position.*/
    private fun moveCameraToUser(){
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    /*Connecting all the polyline's present inside "pathPoints" variable in case of configuration changes.*/
    private fun connectAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    /*Drawing line between two most recent polyline's: Last entry and 2nd last entry.*/
    private fun connectLatestPolyline(){
        if(pathPoints.isNotEmpty()&&pathPoints.last().size > 1 ){
            val preLastLatLang = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLang = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLang)
                .add(lastLatLang)

            map?.addPolyline(polylineOptions)
        }
    }

    private fun zoomOutToSeeWholeTrack(){

        val bounds = LatLngBounds.Builder()

        for(polyline in pathPoints){
            for(coordinates in polyline){
                bounds.include(coordinates)
            }
        }

        //adjusting the height and width of mapView for taking a screen-sort of the whole track ran.
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05f).toInt())
        )

    }

    private fun endRanAndSaveDb(){

        map?.snapshot { bitmap ->
            //our screen-sort image
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculateDistanceFromPolylines(polyline).toInt()
            }

            val averageSpeed = round((distanceInMeters/1000f) / (currentTimMillis/1000f/60/60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = (((distanceInMeters)/1000f) * weight ).toInt()


            val run = Run(bitmap,dateTimeStamp,averageSpeed,distanceInMeters,currentTimMillis,caloriesBurned)
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Data Saved",
                Snackbar.LENGTH_LONG
            ).show()

            stopRun()
        }

    }

    private fun sendCommandToService(action:String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)

        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tracking_toolbar_menu,menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        //Before making  menu item visible,checking if started running or not.
        if (currentTimMillis > 0L)
            this.menu?.getItem(0)?.isVisible = true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.itemCancelTracking -> {
                showCancelDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun showCancelDialog(){

        CancelTrackingDialogFragment().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG)
    }

    private fun stopRun(){
        tvTimer.text = "00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mapView.onSaveInstanceState(outState)
    }
}