package uz.smartarena.mytaxitest.view_model

import androidx.lifecycle.LiveData
import uz.smartarena.mytaxitest.data.database.entity.Location
import uz.smartarena.mytaxitest.view_model.impl.Mode

interface MainViewModel {
    val zoomLiveData: LiveData<Double>
    val myLocationLiveData: LiveData<Pair<Double, Double>>
    val startServiceLiveData: LiveData<Unit>
    val requestPermissionLiveData: LiveData<Unit>
    val setLightTeme: LiveData<Unit>
    val setDarkTeme: LiveData<Unit>
    val setLocations: LiveData<Set<Location>>
    val removeLocations: LiveData<Set<Location>>
    fun startService()
    fun clickZoomIn()
    fun clickZoomOut()
    fun changeCameraPosition(zoom: Double)
    fun setLastLocation(location: Location)
    fun clickMyLocation(isGranted:Boolean)
    fun saveLastLocation()
    fun changeTheme(mode: Mode)
    fun clickHistoryShow()
}