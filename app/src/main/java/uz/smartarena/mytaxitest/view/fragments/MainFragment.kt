package uz.smartarena.mytaxitest.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mapbox.android.core.location.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.lineProgress
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.locationcomponent.location
import uz.smartarena.mytaxitest.R
import uz.smartarena.mytaxitest.data.database.entity.Location
import uz.smartarena.mytaxitest.databinding.FragmentMainBinding
import uz.smartarena.mytaxitest.service.LocationService
import uz.smartarena.mytaxitest.view.Activity
import uz.smartarena.mytaxitest.view_model.MainViewModel
import uz.smartarena.mytaxitest.view_model.impl.MainViewModelImpl
import uz.smartarena.mytaxitest.view_model.impl.Mode


class MainFragment : Fragment(R.layout.fragment_main) {
    private val viewModel: MainViewModel by viewModels<MainViewModelImpl>()
    private lateinit var binding: FragmentMainBinding
    private lateinit var locationEngine: LocationEngine
    var mLocationService: LocationService = LocationService()
    lateinit var mServiceIntent: Intent
    lateinit var mActivity: FragmentActivity
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMainBinding.bind(view)
        mActivity = requireActivity()
        loadViews()
        setObservers()
    }


    private val cameraChangeListener = OnCameraChangeListener { eventData ->
        viewModel.changeCameraPosition(binding.map.getMapboxMap().cameraState.zoom)
    }

    private fun addAnnotationToMap(location: Location) {
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.dot
        )?.let {
            val annotationApi = binding.map.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(location.longitude, location.latitude))
                .withIconImage(it)
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    private fun loadViews() {
        binding.map.getMapboxMap().loadStyleUri(Style.LIGHT) {
            binding.map.location.updateSettings {
                enabled = true
                pulsingEnabled = false
            }
        }
        binding.map.getMapboxMap().addOnCameraChangeListener(cameraChangeListener)
        binding.location.setOnClickListener { startLocationPermissionRequest() }
        binding.zoomIn.setOnClickListener { viewModel.clickZoomIn() }
        binding.zoomOut.setOnClickListener { viewModel.clickZoomOut() }
        val image = AppCompatResources.getDrawable(requireContext(), R.drawable.car)
        binding.map.location.locationPuck = LocationPuck2D(topImage = image, scaleExpression = interpolate {
            linear()
            zoom()
            stop {
                literal(1)
                literal(0.3)
            }
            stop {
                literal(20.0)
                literal(1)
            }
        }.toJson())
    }

    private fun setObservers() {
        viewModel.zoomLiveData.observe(viewLifecycleOwner, zoomObserver)
        viewModel.myLocationLiveData.observe(viewLifecycleOwner, myLocationObserver)
        viewModel.startServiceLiveData.observe(viewLifecycleOwner, startServiceObserver)
        viewModel.setDarkTeme.observe(viewLifecycleOwner, darkModeObserver)
        viewModel.setLightTeme.observe(viewLifecycleOwner, lightModeObserver)
        viewModel.setlocations.observe(viewLifecycleOwner,setLocationObserver)
    }

    private val requestPermissionObserver = Observer<Unit>() {

    }

    private val zoomObserver = Observer<Double> {
        binding.map.getMapboxMap().easeTo(CameraOptions.Builder().zoom(it).build(),
            MapAnimationOptions.mapAnimationOptions {
                lineProgress()
                zoom()
                duration(500)
            })
    }

    private val startServiceObserver = Observer<Unit> {
        if (!Util.isLocationEnabledOrNot(mActivity)) {
            Util.showAlertLocation(
                mActivity,
                getString(R.string.gps_enable),
                getString(R.string.please_turn_on_gps),
                getString(R.string.ok)
            )
        }
        mLocationService = LocationService()
        mServiceIntent = Intent(requireContext(), mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
            requireActivity().startService(mServiceIntent)
        }
    }

    private val darkModeObserver = Observer<Unit> {
        binding.map.getMapboxMap().loadStyleUri(Style.DARK) {
            binding.map.location.updateSettings {
                enabled = true
                pulsingEnabled = false
            }
        }
    }

    private val lightModeObserver = Observer<Unit> {
        binding.map.getMapboxMap().loadStyleUri(Style.LIGHT) {
            binding.map.location.updateSettings {
                enabled = true
                pulsingEnabled = false
            }
        }
    }

    private val setLocationObserver = Observer<Set<Location>> {
        it.forEach {
            addAnnotationToMap(it)
        }
    }

    private val myLocationObserver = Observer<Pair<Double, Double>> {
        binding.map.getMapboxMap().easeTo(CameraOptions.Builder().zoom(17.0).center(Point.fromLngLat(it.first, it.second)).build(), MapAnimationOptions.mapAnimationOptions {
            zoom()
            duration(3500)
        })
    }

    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onSuccess(result: LocationEngineResult) {
            result.lastLocation?.let {
                viewModel.setLastLocation(Location(0, it.longitude, it.latitude, System.currentTimeMillis()))
                viewModel.clickMyLocation()
            }
        }

        override fun onFailure(exception: Exception) {}
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            locationListener()
            viewModel.clickMyLocation()
        } else {
            Toast.makeText(requireContext(), "Joylashuvga ruxsat olinmadi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocationPermissionRequest() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun locationListener() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(requireContext())
        val request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS).setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine.getLastLocation(callback)
    }

    override fun onDestroy() {
        viewModel.saveLastLocation()
        binding.map.getMapboxMap().removeOnCameraChangeListener(cameraChangeListener)
        super.onDestroy()
    }

    override fun onResume() {
        when (requireContext().resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                viewModel.changeTheme(Mode.DARK)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                viewModel.changeTheme(Mode.LIGHT)
            }
        }
        super.onResume()
    }
}
