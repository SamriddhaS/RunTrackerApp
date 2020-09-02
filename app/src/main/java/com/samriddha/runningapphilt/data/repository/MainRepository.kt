package com.samriddha.runningapphilt.data.repository

import com.samriddha.runningapphilt.data.db.Run
import com.samriddha.runningapphilt.data.db.RunDao
import javax.inject.Inject

class MainRepository
@Inject
constructor(private val runDao: RunDao)
{
    suspend fun insertRun(run:Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunSortedByDate() = runDao.getAllRunSortedByDate()

    fun getAllRunSortedByAverageSpeed() = runDao.getAllRunSortedByAverageSpeed()

    fun getAllRunSortedByCalories() = runDao.getAllRunSortedByCalories()

    fun getAllRunSortedByDistance() = runDao.getAllRunSortedByDistance()

    fun getAllRunSortedByTimeInMills() = runDao.getAllRunSortedByTimeInMills()

    fun getTotalTimeInMills() = runDao.getTotalTimeInMills()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistance() = runDao.getTotalDistance()
}