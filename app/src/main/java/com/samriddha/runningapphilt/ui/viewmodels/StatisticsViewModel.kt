package com.samriddha.runningapphilt.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.samriddha.runningapphilt.data.repository.MainRepository


class StatisticsViewModel
@ViewModelInject
constructor(val mainRepository: MainRepository)
    : ViewModel() {

    val totalTimeRan = mainRepository.getTotalTimeInMills()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunSortedByDate()

}