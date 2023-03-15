package uz.smartarena.mytaxitest.view

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uz.smartarena.mytaxitest.R
import uz.smartarena.mytaxitest.service.LocationService


class Activity : AppCompatActivity() {
    var mLocationService: LocationService = LocationService()
    lateinit var mServiceIntent: Intent
    lateinit var mActivity: Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mActivity = this

        if (!Util.isLocationEnabledOrNot(mActivity)) {
            Util.showAlertLocation(
                mActivity,
                getString(R.string.gps_enable),
                getString(R.string.please_turn_on_gps),
                getString(R.string.ok)
            )
        }

        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
            startService(mServiceIntent)
        }
    }
}