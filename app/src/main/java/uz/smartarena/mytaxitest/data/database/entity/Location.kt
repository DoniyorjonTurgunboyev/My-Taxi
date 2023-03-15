package uz.smartarena.mytaxitest.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val longitude: Double,
    val latitude: Double,
    val time: Long
)
