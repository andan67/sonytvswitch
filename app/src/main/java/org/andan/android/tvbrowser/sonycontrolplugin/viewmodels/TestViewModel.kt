package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.di.DaggerApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyRepository

class TestViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val TAG = TestViewModel::class.java.name
    // repository for control data
    private var repository: SonyRepository = SonyControlApplication.get().appComponent.sonyRepository()
    // val sampleText = "This is a sample text"

    /*val powerStatus : LiveData<String> = liveData(Dispatchers.IO) {
        val result = repository.getPowerStatus()
        emit(result)
    }*/

   /* fun getCurrentTime() = liveData(Dispatchers.IO) {
        //val result = repository.getCurrentTime()
        // Log.d(TAG,"currentTime: " + result)
        emit(repository.getCurrentTime())
    }*/

    val currentTime = repository.currentTime
    fun fetchCurrentTime() = viewModelScope.launch(Dispatchers.IO) {
        //val result = repository.getCurrentTime()
        // Log.d(TAG,"currentTime: " + result)
        repository.getCurrentTime()
    }

    val playingContentInfo = repository.playingContentInfo
    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        //val result = repository.getCurrentTime()
        // Log.d(TAG,"currentTime: " + result)
        repository.getPlayingContentInfo()
    }

    //fun registerControl()

//    fun getCurrentTime() = repository.getCurrentTime()

//val currentTime = repository.currentTimeString

//fun getCurrentTime(): LiveData<String> = repository.getCurrentTime()

//val currentTimeString = getCurrentTime()
}
