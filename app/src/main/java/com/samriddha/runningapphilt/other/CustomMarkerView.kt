package com.samriddha.runningapphilt.other

import android.content.Context
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.samriddha.runningapphilt.data.db.Run
import kotlinx.android.synthetic.main.marker_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs: List<Run>,
    context: Context,
    layoutId: Int
):MarkerView(context,layoutId) {


    override fun getOffset():MPPointF{
        return MPPointF(-width/2f,-height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if (e==null)
            return

        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]


        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp
        }
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calender.time)


        val avgSpeed = "${run.avgSpeedKPH}km/h"
        tvAvgSpeed.text = avgSpeed


        val distance = "${run.distanceInMeter}km"
        tvDistance.text = distance


        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMills)


        val caloriesBur = "${run.burnedCalories}kcal"
        tvCaloriesBurned.text = caloriesBur
    }

}