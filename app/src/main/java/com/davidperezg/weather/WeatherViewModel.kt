package com.davidperezg.weather

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperezg.weather.data.TemperatureUnit
import com.davidperezg.weather.data.AppTheme
import com.davidperezg.weather.data.UserLocation
import com.davidperezg.weather.data.WeatherAppRepository
import com.davidperezg.weather.ui.UiEvent
import com.davidperezg.weather.ui.ViewModelEvent
import com.davidperezg.weather.util.LocationReceiver
import com.davidperezg.weather.util.Routes
import com.davidperezg.weather.util.SharedPreferencesUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.IOException

class WeatherViewModel(
    private val context: Application,
    private val repository: WeatherAppRepository,
    private val spUtil: SharedPreferencesUtil,
    locationReceiver: LocationReceiver,
) : ViewModel() {
    val weatherForecast = repository.getWeatherForecast()

    private var location: UserLocation? = spUtil.getLastLocation()
    var temperatureUnit by mutableStateOf(spUtil.getTemperatureUnit())
    var appTheme by mutableStateOf(spUtil.getTheme())

    private val _uiEvent = Channel<ViewModelEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadConfiguration()
        locationReceiver.subscribe(this::updateLocation)
    }

    fun loadConfiguration() {
        temperatureUnit = spUtil.getTemperatureUnit()
        appTheme = spUtil.getTheme()
        location = spUtil.getLastLocation()

        viewModelScope.launch {
            repository.setTemperatureUnit(temperatureUnit)
        }

        applyTheme(appTheme)
    }

    private fun setUiTheme(theme: AppTheme) {
        if (theme == appTheme) return

        appTheme = if (appTheme == AppTheme.LIGHT) {
            AppTheme.DARK
        } else {
            AppTheme.LIGHT
        }

        spUtil.saveTheme(appTheme)
        applyTheme(appTheme)
    }

    private fun applyTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun useTemperatureUnit(unit: TemperatureUnit) {
        // Nothing to change
        if (unit == temperatureUnit) return

        temperatureUnit = if (temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }

        spUtil.saveTemperatureUnit(temperatureUnit)
        viewModelScope.launch {
            repository.setTemperatureUnit(temperatureUnit)
        }
    }

    private fun updateLocation(location: Location) {
        this.location = UserLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )
        spUtil.saveLastLocation(this.location!!)
        Log.i(TAG, "updateUserLocation: ${this.location}")
    }

    private fun sendEventToUi(event: ViewModelEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }


    fun onUiEvent(event: UiEvent) {
        when (event) {
            UiEvent.SettingsIconClick -> sendEventToUi(ViewModelEvent.Navigate(Routes.SETTINGS))

            UiEvent.UpdateForecast -> {
                viewModelScope.launch {
                    handleOnUpdateForecastEvent()
                }
            }

            is UiEvent.SetAppTheme -> setUiTheme(event.theme)
            is UiEvent.SetTemperatureUnit -> useTemperatureUnit(event.unit)
        }
    }

    private suspend fun handleOnUpdateForecastEvent() {
        if (location == null) {
            sendEventToUi(
                ViewModelEvent.ShowSnackbar(
                    message = context.getString(R.string.location_unavailable),
                )
            )
            return
        }

        sendEventToUi(
            ViewModelEvent.ShowSnackbar(
                message = context.getString(R.string.updating),
            )
        )

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable is IOException) {
                Log.e(TAG, "handleOnUpdateForecastEvent: Network error", throwable)
            } else {
                Log.e(TAG, "handleOnUpdateForecastEvent: Unknown error", throwable)
            }

            sendEventToUi(
                ViewModelEvent.ShowSnackbar(
                    message = context.getString(R.string.unable_to_fetch_weather_data),
                    action = context.getString(R.string.retry_fetch)
                )
            )
        }

        val job = viewModelScope.launch(errorHandler) {
            repository.fetchForecast(WEATHER_API_KEY, location!!)
        }

        job.invokeOnCompletion { exception ->
            if (exception != null) {
                // The job finished with an error
                return@invokeOnCompletion
            }

            sendEventToUi(
                ViewModelEvent.ShowSnackbar(
                    message = context.getString(R.string.forecast_updated),
                )
            )
        }
    }

    companion object {
        const val TAG = "WeatherViewModel"
        private const val WEATHER_API_KEY = "66e299409de74d0ca49151039241306" // WeatherAPI.com
    }
}