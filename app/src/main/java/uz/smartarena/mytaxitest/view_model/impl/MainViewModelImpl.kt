package uz.smartarena.mytaxitest.view_model.impl

import androidx.lifecycle.LiveData
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
    private var themeMode: Mode = Mode.LIGHT
    override val zoomLiveData = MutableLiveData<Double>()
    override val myLocationLiveData = MutableLiveData<Pair<Double, Double>>()
    override val startServiceLiveData = MutableLiveData<Unit>()
    override val requestPermissionLiveData = MutableLiveData<Unit>()
    override val setLightTeme = MutableLiveData<Unit>()
    override val setDarkTeme = MutableLiveData<Unit>()
    override val setlocations = MutableLiveData<Set<Location>>()

    override fun startService(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            startServiceLiveData.value = Unit
        } else {
            requestPermissionLiveData.value = Unit
        }
    }

    init {
        viewModelScope.launch {
            val locations = repository.getAllLocations()
            setlocations.value = locations.toSet()
        }
        val lastLocPair = repository.getLastLocation()
        lastLocation = Location(0, lastLocPair.first, lastLocPair.second, 0)
        myLocationLiveData.value = lastLocPair
        startServiceLiveData.value = Unit
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
    }

    override fun clickMyLocation() {
        myLocationLiveData.value = Pair(lastLocation.longitude, lastLocation.latitude)
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
}