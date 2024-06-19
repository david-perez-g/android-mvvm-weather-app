package com.davidperezg.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidperezg.weather.ui.settings_screen.SettingsScreen
import com.davidperezg.weather.ui.theme.WeatherTheme
import com.davidperezg.weather.ui.weather_app_screen.WeatherAppScreen
import com.davidperezg.weather.util.LocationReceiver
import com.davidperezg.weather.util.Routes
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val weatherViewModel by viewModel<WeatherViewModel>()

    private val locationReceiver: LocationReceiver by inject()

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

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        try {
            locationReceiver.startReceivingUpdates()
        } catch (e: SecurityException) {
            Log.e(TAG, "startLocationUpdates: ${e.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                            viewModel = weatherViewModel,
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