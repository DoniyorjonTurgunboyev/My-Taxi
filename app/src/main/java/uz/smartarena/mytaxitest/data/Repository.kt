package uz.smartarena.mytaxitest.data

import uz.smartarena.mytaxitest.data.database.AppDatabase
import uz.smartarena.mytaxitest.data.database.dao.LocationDao
import uz.smartarena.mytaxitest.data.database.entity.Location
import uz.smartarena.mytaxitest.data.local.LocalMemory

class Repository private constructor() {
    private lateinit var database: AppDatabase
    private lateinit var locationDao: LocationDao
    private lateinit var localMemory: LocalMemory

    fun saveLocation(location: Location) = locationDao.saveLocation(location)
    fun saveLastLocationToLocal(location: Pair<Double, Double>) {
        localMemory.lastLocation = location
    }

    suspend fun getAllLocations() = locationDao.getAllLocations()

    fun getLastLocation(): Pair<Double, Double> = localMemory.lastLocation

    companion object {
        private lateinit var instance: Repository
        fun getInstance(): Repository {
            if (!this::instance.isInitialized) {
                instance = Repository()
                instance.database = AppDatabase.getInstance()
                instance.locationDao = instance.database.locationDao()
                instance.localMemory = LocalMemory()
            }
            return instance
        }
    }
}