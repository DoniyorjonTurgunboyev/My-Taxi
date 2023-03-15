package uz.smartarena.mytaxitest.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import uz.smartarena.mytaxitest.data.database.entity.Location

@Dao
interface LocationDao {
    @Insert
    fun saveLocation(location: Location)

    @Query("Select * from Location Limit 100")
    suspend fun getAllLocations(): List<Location>
}