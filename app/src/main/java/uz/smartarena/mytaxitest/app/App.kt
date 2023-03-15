package uz.smartarena.mytaxitest.app

import android.app.Application
import android.util.Log

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}