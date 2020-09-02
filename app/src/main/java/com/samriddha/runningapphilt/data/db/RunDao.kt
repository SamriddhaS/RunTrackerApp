package com.samriddha.runningapphilt.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run:Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM run_table ORDER BY timeStamp DESC")
    fun getAllRunSortedByDate():LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY timeInMills DESC")
    fun getAllRunSortedByTimeInMills():LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY burnedCalories DESC")
    fun getAllRunSortedByCalories():LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY avgSpeedKPH DESC")
    fun getAllRunSortedByAverageSpeed():LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY distanceInMeter DESC")
    fun getAllRunSortedByDistance():LiveData<List<Run>>

    @Query("SELECT SUM(timeInMills) FROM run_table")
    fun getTotalTimeInMills():LiveData<Long>

    @Query("SELECT SUM(burnedCalories) FROM run_table")
    fun getTotalCaloriesBurned():LiveData<Int>

    @Query("SELECT SUM(distanceInMeter) FROM run_table")
    fun getTotalDistance():LiveData<Int>

    @Query("SELECT AVG(avgSpeedKPH) FROM run_table")
    fun getTotalAvgSpeed():LiveData<Float>



}