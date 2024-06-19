package com.davidperezg.weather

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidperezg.weather.data.Temperature
import com.davidperezg.weather.data.TemperatureUnit
import com.davidperezg.weather.data.AppTheme
import com.davidperezg.weather.data.UserLocation
import com.davidperezg.weather.data.WeatherAppRepository
import com.davidperezg.weather.data.WeatherCondition
import com.davidperezg.weather.data.WeatherCurrentState
import com.davidperezg.weather.data.WeatherDayResume
import com.davidperezg.weather.data.WeatherForecast
import com.davidperezg.weather.util.SharedPreferencesUtil
import com.davidperezg.weather.util.WeekDay
import com.davidperezg.weather.weatherapi.WeatherApi
import com.davidperezg.weather.weatherapi.WeatherApiResponseBodyParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException
import java.util.Date

val emptyWeatherForecast = WeatherForecast(
    "-", "-", WeatherCurrentState(
        date = Date(0),
        temperature = Temperature(0.0),
        temperatureFeelsLike = Temperature(0.0),
        condition = WeatherCondition("-", 0),
        humidity = 50,
    ),
    days = listOf(),
    today = WeatherDayResume(
        date = Date(0),
        condition = WeatherCondition("-", 0),
        hours = listOf(),
        minimumTemperature = Temperature(0.0),
        maximumTemperature = Temperature(0.0),
        weekDay = WeekDay.FRIDAY,
    ), following24Hours = listOf()
)

class WeatherViewModel(
    private val repository: WeatherAppRepository,
    private val spUtil: SharedPreferencesUtil,
    private val weatherApi: WeatherApi,
    private val apiResponseParser: WeatherApiResponseBodyParser,
) : ViewModel() {
    private val _weatherForecast = mutableStateOf(emptyWeatherForecast)
    val weatherForecast by _weatherForecast

    private var userLocation: UserLocation? = null
    lateinit var temperatureUnit : TemperatureUnit
    lateinit var appTheme : AppTheme

    init {
        loadConfiguration()
    }

    fun loadConfiguration() {
        appTheme = spUtil.getTheme()
        userLocation = spUtil.getLocation()
        temperatureUnit = spUtil.getTemperatureUnit()

        viewModelScope.launch {
            _weatherForecast.value = repository.getWeatherForecast() ?: emptyWeatherForecast
            applyTemperatureUnit(temperatureUnit)
        }

        applyTheme(appTheme)
        apiResponseParser.useTemperatureUnit(temperatureUnit)
    }

    fun switchUITheme() {
        appTheme = if (appTheme == AppTheme.LIGHT) {
            AppTheme.DARK
        } else {
            AppTheme.LIGHT
        }

        spUtil.saveTheme(appTheme)

        applyTheme(appTheme)
    }

    fun useTemperatureUnit(unit: TemperatureUnit) {
        // Nothing to change
        if (unit == temperatureUnit) return

        temperatureUnit = if (temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }

        spUtil.saveTemperatureUnit(temperatureUnit)
        apiResponseParser.useTemperatureUnit(temperatureUnit)
        applyTemperatureUnit(temperatureUnit)
    }

    private fun applyTemperatureUnit(unit: TemperatureUnit) {
        weatherForecast.apply {
            currentState.temperature.useTemperatureUnit(unit)
            currentState.temperatureFeelsLike.useTemperatureUnit(unit)

            days.forEach {
                it.hours.forEach { hour -> hour.temperature.useTemperatureUnit(unit) }
                it.minimumTemperature.useTemperatureUnit(unit)
                it.maximumTemperature.useTemperatureUnit(unit)
            }
            today.minimumTemperature.useTemperatureUnit(unit)
            today.maximumTemperature.useTemperatureUnit(unit)

            following24Hours.forEach { it.temperature.useTemperatureUnit(unit) }
            viewModelScope.launch {
                repository.insertOrUpdateWeatherForecast(weatherForecast)
            }
        }
    }

    private fun applyTheme(theme: AppTheme) {
        when (theme) {
            AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun updateUserLocation(location: UserLocation) {
        userLocation = location
        spUtil.saveLocation(userLocation!!)
        Log.i(TAG, "updateUserLocation: $userLocation")
    }

    fun updateForecast(onError: (e: Exception) -> Unit) {
        if (userLocation == null) {
            Log.e(TAG, "updateForecast: No user location")
            throw IllegalStateException("No user location provided")
        }

        viewModelScope.launch {
            Log.i(TAG, "updateForecast: getting forecast")
            val response = try {
                weatherApi.getForecast(API_KEY, userLocation!!.toString())
            } catch (e: Exception) {
                Log.e(TAG, "updateForecast:" + e.message)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                Log.i(TAG, "updateForecast: got a successful response!")
                _weatherForecast.value = apiResponseParser.parse(response.body()!!)
                repository.insertOrUpdateWeatherForecast(weatherForecast)
            }
        }
    }

    companion object {
        const val TAG = "WeatherViewModel"
        private const val API_KEY = "66e299409de74d0ca49151039241306" // WeatherAPI.com
    }
}