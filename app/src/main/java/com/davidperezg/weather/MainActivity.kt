package com.davidperezg.weather

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.davidperezg.weather.ui.theme.WeatherTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

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

    private fun setupUI() {
        setContent {
            WeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainUI(this@MainActivity, weatherViewModel) { requestLocationPermission() }
                }
            }
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
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        weatherViewModel.loadConfiguration()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(context: Context, vm: WeatherViewModel, onLocationPermissionsNeeded: () -> Unit) {
    val isRefreshing = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WeatherApp",
                        style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        // Start SettingsActivity
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        },
    ) { paddingValues ->

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing.value),
            onRefresh = {
                try {
                    Toast
                        .makeText(
                            context,
                            R.string.updating,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    vm.updateForecast(onError = {
                        Toast
                            .makeText(
                                context,
                                R.string.unable_to_fetch_weather_data,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    })
                } catch (e: IllegalStateException) {
                    onLocationPermissionsNeeded()
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = 10.dp
                    )
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    thickness = 3.dp
                )

                Spacer(modifier = Modifier.height(15.dp))
                CurrentWeatherState(vm)
                Spacer(modifier = Modifier.height(15.dp))
                HourForecast(vm)
                Spacer(modifier = Modifier.height(15.dp))
                WeekForecast(vm)
            }
        }
    }

}

@Composable
fun CurrentWeatherState(vm: WeatherViewModel) {
    Row {
        Text(
            text = "${vm.weatherForecast.city}, ${vm.weatherForecast.country}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "location icon"
        )
    }
    
    Spacer(modifier = Modifier.height(10.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row {
            Text(
                text = vm.weatherForecast.currentState.temperature.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row {
            Text(
                text =
                "${vm.weatherForecast.currentState.condition.text}, " +
                "${vm.weatherForecast.today.minimumTemperature} / ${vm.weatherForecast.today.maximumTemperature}, \n"
                + stringResource(R.string.temp_feels_like) + " ${vm.weatherForecast.currentState.temperatureFeelsLike}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun HourForecast(vm: WeatherViewModel) {
    LazyRow(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium
            )
            .height(90.dp)
            .fillMaxWidth()
    ) {
        items(vm.weatherForecast.following24Hours) { hour ->
            HourResume(hour)
        }
    }
}

@Composable
fun HourResume(hour: WeatherHourResume) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatHourFromDate(hour.date),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Image(
                painterResource(id = hour.condition.imageResourceId),
                contentDescription = hour.condition.text,
                modifier = Modifier
                    .size(30.dp)
                    .fillMaxWidth()
            )

        }

        Text(
            text = hour.temperature.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WeekForecast(vm: WeatherViewModel) {
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        for (day in vm.weatherForecast.days) {
            DayResume(day)
        }
    }
}

@Composable
fun DayResume(day: WeatherDayResume) {
    Row(
        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
        verticalAlignment = CenterVertically
    ) {
        Text(
            text = formatDayFromDate(day.date),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.15f)
        )

        Text(
            text = stringResource(day.weekDay.getStringResourceId()),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.30f)
        )

        Image(
            painter = painterResource(id = day.condition.imageResourceId),
            contentDescription = day.condition.text,
            modifier = Modifier
                .weight(0.10f)
                .align(CenterVertically)
        )

        Text(
            text = "${day.minimumTemperature} / ${day.maximumTemperature}",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.35f)
        )
    }
}
