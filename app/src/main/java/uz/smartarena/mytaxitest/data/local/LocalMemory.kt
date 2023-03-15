package uz.smartarena.mytaxitest.data.local

import android.content.Context
import uz.smartarena.mytaxitest.app.App

class LocalMemory {
    //default location for first load map camera position
    private var latitude = 41.311110
    private var longitude = 69.279734
    private val sharedPreferences = App.instance.getSharedPreferences("LocalMemory", Context.MODE_PRIVATE)
    var lastLocation: Pair<Double, Double>
        get() {
            return Pair(
                sharedPreferences.getString("longitude", longitude.toString())!!.toDouble(),
                sharedPreferences.getString("latitude", latitude.toString())!!.toDouble()
            )
        }
        set(value) {
            sharedPreferences.edit().apply {
                putString("longitude", value.first.toString())
                putString("latitude", value.second.toString())
            }.apply()
        }
}