package com.samriddha.runningapphilt.data.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    var image:Bitmap? = null,
    var timeStamp:Long = 0L,
    var avgSpeedKPH:Float = 0f,
    var distanceInMeter:Int=0,
    var timeInMills:Long=0L,
    var burnedCalories:Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var primaryKey:Int? = null
}