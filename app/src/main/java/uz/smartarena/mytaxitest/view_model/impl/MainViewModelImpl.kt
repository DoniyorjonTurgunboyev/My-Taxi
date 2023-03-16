package uz.smartarena.mytaxitest.view_model.impl

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uz.smartarena.mytaxitest.data.Repository
import uz.smartarena.mytaxitest.data.database.entity.Location
import uz.smartarena.mytaxitest.view_model.MainViewModel

class MainViewModelImpl : MainViewModel, ViewModel() {
    private var lastLocation: Location
    private val repository = Repository.getInstance()
    private var zoomValue = 17.0
    private var firstMyLocationClick = true
    private var showHistory = false
    private var isStartServise = false
    private lateinit var locationHistory: Set<Location>
    private var themeMode: Mode = Mode.LIGHT
    override val zoomLiveData = MutableLiveData<Double>()
    override val myLocationLiveData = MutableLiveData<Pair<Double, Double>>()
    override val startServiceLiveData = MutableLiveData<Unit>()
    override val requestPermissionLiveData = MutableLiveData<Unit>()
    override val setLightTeme = MutableLiveData<Unit>()
    override val setDarkTeme = MutableLiveData<Unit>()
    override val setLocations = MutableLiveData<Set<Location>>()
    override val removeLocations = MutableLiveData<Set<Location>>()

    override fun startService() {
        if (!isStartServise) {
            isStartServise = true
            startServiceLiveData.value = Unit
        }
    }

    init {
        if (!repository.getIsFirst()) {
            startService()
        }
        val lastLocPair = repository.getLastLocation()
        lastLocation = Location(0, lastLocPair.first, lastLocPair.second, 0)
        myLocationLiveData.value = lastLocPair
    }

    override fun clickZoomIn() {
        zoomValue++
        zoomLiveData.value = zoomValue
    }

    override fun clickZoomOut() {
        zoomValue--
        zoomLiveData.value = zoomValue
    }

    override fun changeCameraPosition(zoom: Double) {
        zoomValue = zoom
    }

    override fun setLastLocation(location: Location) {
        lastLocation = location
        if (firstMyLocationClick) {
            myLocationLiveData.value = Pair(lastLocation.longitude, lastLocation.latitude)
            firstMyLocationClick = false
        }
    }

    override fun clickMyLocation(isGranted: Boolean) {
        if (isGranted) {
            repository.setIsFirst(false)
            myLocationLiveData.value = Pair(lastLocation.longitude, lastLocation.latitude)
        } else {
            requestPermissionLiveData.value = Unit
        }
    }

    override fun saveLastLocation() {
        repository.saveLastLocationToLocal(Pair(lastLocation.longitude, lastLocation.latitude))
    }

    override fun changeTheme(mode: Mode) {
        if (themeMode != mode) {
            when (mode) {
                Mode.DARK -> setDarkTeme.value = Unit
                Mode.LIGHT -> setLightTeme.value = Unit
            }
            themeMode = mode
        }
    }

    override fun clickHistoryShow() {
        if (showHistory) {
            removeLocations.value = locationHistory
            showHistory = false
        } else {
            showHistory = true
            viewModelScope.launch {
                //Ushbu kod qismi qo'shimcha feature uchun
                //Ya'ni chaqmoqni bosganda history ma'lumtlarini kartada aks ettirish uchun
                val locations = repository.getAllLocations()
                locations.map { t ->
                    t.latitude = String.format("%.5f", t.latitude).replace(',', '.').toDouble()
                    t.longitude = String.format("%.5f", t.longitude).replace(',', '.').toDouble()
                }
                Log.d("TTT", "clickHistoryShow: $locations")
                //latitude va longitude bir xil bo'lgan elementlari equalsni override qilish orqali setda o'chirib yuborildi
                locationHistory = locations.toSet()
                setLocations.value = locationHistory
            }
        }
    }
}