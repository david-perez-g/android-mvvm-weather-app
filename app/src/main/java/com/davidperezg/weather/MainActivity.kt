package com.davidperezg.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidperezg.weather.data.UserLocation
import com.davidperezg.weather.ui.settings_screen.SettingsScreen
import com.davidperezg.weather.ui.theme.WeatherTheme
import com.davidperezg.weather.ui.weather_app_screen.WeatherAppScreen
import com.davidperezg.weather.util.Routes

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val weatherViewModel by viewModels<WeatherViewModel>()

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun initLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun initLocationListener() {
        locationListener = LocationListener {
            Log.i(TAG, "onLocationResult: $it")
            weatherViewModel.updateUserLocation(
                UserLocation(
                    it.latitude,
                    it.longitude,
                )
            )
        }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates: Starting")
        try {
            locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        } catch (e: SecurityException) {
            Log.e(TAG, "startLocationUpdates: ${e.message}")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLocationManager()
        initLocationListener()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }

        setupUI()
    }

    private fun setupUI() {
        setContent {
            WeatherTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.WEATHER_APP
                ) {
                    composable(Routes.WEATHER_APP) {
                        WeatherAppScreen(
                            this@MainActivity,
                            viewModel = weatherViewModel,
                            onLocationPermissionsNeeded = @MainActivity ::requestLocationPermission,
                            onNavigate = { route ->
                                navController.navigate(route)
                            }
                        )
                    }
                    composable(route = Routes.SETTINGS) {
                        SettingsScreen(
                            viewModel = weatherViewModel,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        weatherViewModel.loadConfiguration()
    }
}