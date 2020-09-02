package com.samriddha.runningapphilt.other

import android.graphics.Color

object Constants {
    const val RUN_DATABASE_NAME = "run_database_name"

    const val REQUEST_CODE_LOCATION_PERMISSION = 1

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT ="ACTION_SHOW_TRACKING_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L

    const val TIMER_UPDATE_INTERVAL = 50L

    const val POLYLINE_COLOR = Color.GREEN
    const val POLYLINE_WIDTH = 8f

    const val MAP_ZOOM = 15f

    const val NOTIFICATION_CHANNEL_ID= "TRACKING_CHANNEL"
    const val NOTIFICATION_CHANNEL_NAME= "TRACKING"
    const val NOTIFICATION_ID= 1

    const val SHARED_PREF_NAME = "SHARED_PREF"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_USERNAME = "NAME"
    const val KEY_USER_WEIGHT = "WEIGHT"

    const val CANCEL_TRACKING_DIALOG = "cancel_dialog"
}