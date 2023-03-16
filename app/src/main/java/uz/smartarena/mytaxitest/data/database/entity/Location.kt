package uz.smartarena.mytaxitest.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var longitude: Double,
    var latitude: Double,
    val time: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Location

        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        return result
    }
}