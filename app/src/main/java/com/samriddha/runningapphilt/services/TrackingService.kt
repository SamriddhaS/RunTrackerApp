package com.samriddha.runningapphilt.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.other.Constants.ACTION_PAUSE_SERVICE
import com.samriddha.runningapphilt.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.samriddha.runningapphilt.other.Constants.ACTION_STOP_SERVICE
import com.samriddha.runningapphilt.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.samriddha.runningapphilt.other.Constants.LOCATION_UPDATE_INTERVAL
import com.samriddha.runningapphilt.other.Constants.NOTIFICATION_CHANNEL_ID
import com.samriddha.runningapphilt.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.samriddha.runningapphilt.other.Constants.NOTIFICATION_ID
import com.samriddha.runningapphilt.other.Constants.TIMER_UPDATE_INTERVAL
import com.samriddha.runningapphilt.other.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

typealias Polyline = MutableList<LatLng> // List that stores coordinates(latitude,longitude) of a run
typealias Polylines = MutableList<Polyline> //This list is for saving multiple polyline in a list.User can start and then stop and then start again
// therefore making multiple lines in the map. We want to show every line with gaps as well where user stopped.

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder // for showing initial notification

    lateinit var currentNotificationBuilder: NotificationCompat.Builder // for updating notification after each second

    private val timeRanInSeconds = MutableLiveData<Long>() //for notification purpose

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRanInMillis =
            MutableLiveData<Long>() //This will be accessed from Tracking fragment for updating stopwatch

    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRanInSeconds.postValue(0L)
        timeRanInMillis.postValue(0L)
        currentNotificationBuilder =
            baseNotificationBuilder //initially setting the currentNotification to base notification
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION:${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermission(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {

            when (it.action) {

                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Service Started")
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                        Timber.d("Resume Service")
                    }
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                    Timber.d("Service Stop")
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService()
                    Timber.d("Service Paused")
                }

            }

        }
        return super.onStartCommand(intent, flags, startId)

    }

    private var isTimerEnabled = false
    private var lapTime = 0L // Time of a single lap
    private var timeRan = 0L //Total time ran combining all of our lapTime's
    private var timeStarted = 0L // Time stamp when we start running.
    private var lastSecondTimeStamp = 0L

    private fun startTimer() {
        addEmptyPolyline() //We need to add an empty polyline every time starting a new ran or resuming a run after pausing for some time.
        isTracking.postValue(true) //start tracking user location when user taps start.
        timeStarted = System.currentTimeMillis() //Getting timestamp of starting time of a run.
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime =
                    System.currentTimeMillis() - timeStarted //Time difference between now and start time of the lap.
                timeRanInMillis.postValue(timeRan + lapTime) //Total ran time.This will be shown in the stopwatch in Tracking fragment
                if (timeRanInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timeRanInSeconds.postValue(timeRanInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRan += lapTime
        }

    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startForegroundService() {

        startTimer()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRanInSeconds.observe(this, Observer {

            if(!serviceKilled){
                val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))

                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })

    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }



    private fun updateNotificationTrackingState(isTracking: Boolean) {

        val notificationActionText =
            if (isTracking) "Pause" else "Resume" // for setting action button in notification
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }

}