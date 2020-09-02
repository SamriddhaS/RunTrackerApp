package com.samriddha.runningapphilt.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samriddha.runningapphilt.data.db.Run
import com.samriddha.runningapphilt.data.repository.MainRepository
import com.samriddha.runningapphilt.other.SortType
import kotlinx.coroutines.launch


class MainViewModel
@ViewModelInject
constructor(private val mainRepository: MainRepository) : ViewModel() {

    var sortType = SortType.DATE

    val runs = MediatorLiveData<List<Run>>()

    private val runSortedDate = mainRepository.getAllRunSortedByDate()
    private val runSortedDistance = mainRepository.getAllRunSortedByDistance()
    private val runSortedCalories = mainRepository.getAllRunSortedByCalories()
    private val runSortedTimeInMillie = mainRepository.getAllRunSortedByTimeInMills()
    private val runSortedAvgSpeed = mainRepository.getAllRunSortedByAverageSpeed()

    init {
        /*initially showing the recycler view item's when fragment is loaded first time.*/
        runs.addSource(runSortedDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedCalories) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedTimeInMillie) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        /*for sorting the items with a different sorting type.*/
        SortType.DATE -> runSortedDate.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortedCalories.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortedDistance.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortedTimeInMillie.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedAvgSpeed.value?.let { runs.value = it }
    }.also { this.sortType = sortType }


    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

}