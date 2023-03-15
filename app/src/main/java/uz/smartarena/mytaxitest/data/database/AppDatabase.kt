package uz.smartarena.mytaxitest.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.smartarena.mytaxitest.app.App
import uz.smartarena.mytaxitest.data.database.dao.LocationDao
import uz.smartarena.mytaxitest.data.database.entity.Location

@Database(entities = [Location::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(App.instance, AppDatabase::class.java, "LocationDatabase.db")
                    .build()
            }
            return instance!!
        }
    }
}