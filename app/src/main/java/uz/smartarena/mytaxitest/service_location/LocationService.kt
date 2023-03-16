package uz.smartarena.mytaxitest.service_location

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.smartarena.mytaxitest.R
import uz.smartarena.mytaxitest.data.Repository
import uz.smartarena.mytaxitest.data.database.entity.Location

class LocationService : Service() {
    lateinit var repository: Repository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        repository = Repository.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("My Taxi is working")
            .setContentText("Getting gps location in background")
            .setSmallIcon(R.drawable.location)
            .setOngoing(true)
        locationClient
            .getLocationUpdates(5000)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                repository.saveLocation(Location(0, location.longitude, location.latitude, System.currentTimeMillis()))
            }
            .launchIn(serviceScope)
        startForeground(1, notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}