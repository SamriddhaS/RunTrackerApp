package com.samriddha.runningapphilt.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.samriddha.runningapphilt.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import java.util.concurrent.TimeUnit

object TrackingUtility {

    //This function will tell us if the app already has the required permissions.
    fun hasLocationPermission(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getFormattedStopWatchTime(time: Long, includeMillie: Boolean = false): String {

        var currentMillie = time

        val hour = TimeUnit.MILLISECONDS.toHours(currentMillie)
        currentMillie -= TimeUnit.HOURS.toMillis(hour)

        val minute = TimeUnit.MILLISECONDS.toMinutes(currentMillie)
        currentMillie -= TimeUnit.MINUTES.toMillis(minute)

        val second = TimeUnit.MILLISECONDS.toSeconds(currentMillie)
        if (!includeMillie) {
            return "${if (hour < 10) "0" else ""}$hour:" +
                    "${if (minute < 10) "0" else ""}$minute:" +
                    "${if (second < 10) "0" else ""}$second"
        }

        currentMillie -= TimeUnit.SECONDS.toMillis(second)
        currentMillie /= 10 // we want to show only 2 digits of milliseconds
        return "${if (hour < 10) "0" else ""}$hour:" +
                "${if (minute < 10) "0" else ""}$minute:" +
                "${if (second < 10) "0" else ""}$second:" +
                "${if (currentMillie < 10) "0" else ""}$currentMillie"

    }

    fun calculateDistanceFromPolylines(polyline: Polyline):Float{

        var distance = 0f
        for ( i in 0..polyline.size -2){

            val pos1 = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance +=result[0]
        }
        return distance
    }


}