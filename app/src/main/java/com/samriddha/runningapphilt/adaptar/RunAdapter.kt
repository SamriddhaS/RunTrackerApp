package com.samriddha.runningapphilt.adaptar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.data.db.Run
import com.samriddha.runningapphilt.other.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>()  {

    inner class RunViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    private val diffCallBack = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.primaryKey == newItem.primaryKey
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }

    }

    private val differ = AsyncListDiffer(this,diffCallBack)

    fun submitList(listRun:List<Run>) = differ.submitList(listRun)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.item_run,
            parent,
            false
        ))
    }

    override fun getItemCount(): Int {
       return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {

        val run = differ.currentList[position]
        holder.itemView.apply {

            Glide.with(this).load(run.image).into(ivRunImage)


            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd/MM/yy",Locale.getDefault())
            tvDate.text = dateFormat.format(calender.time)


            val avgSpeed = "${run.avgSpeedKPH}km/h"
            tvAvgSpeed.text = avgSpeed


            val distance = "${run.distanceInMeter}km"
            tvDistance.text = distance


            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMills)


            val caloriesBur = "${run.burnedCalories}kcal"
            tvCalories.text = caloriesBur

        }

    }
}