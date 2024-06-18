package com.davidperezg.weather

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.davidperezg.weather.data.Temperature
import com.davidperezg.weather.data.TemperatureUnit
import com.davidperezg.weather.data.AppTheme
import com.davidperezg.weather.data.UserLocation
import com.davidperezg.weather.data.WeatherAppDatabase
import com.davidperezg.weather.data.WeatherAppRepository
import com.davidperezg.weather.data.WeatherCondition
import com.davidperezg.weather.data.WeatherCurrentState
import com.davidperezg.weather.data.WeatherDayResume
import com.davidperezg.weather.data.WeatherForecast
import com.davidperezg.weather.util.SharedPreferencesUtil
import com.davidperezg.weather.util.WeekDay
import com.davidperezg.weather.weatherapi.WeatherApiResponseBodyParserImpl
import com.davidperezg.weather.weatherapi.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
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

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val TAG = "WeatherViewModel"
        const val DB_NAME = "WeatherAppDatabase"
        private const val API_KEY = "66e299409de74d0ca49151039241306" // WeatherAPI.com
    }

    private val _weatherForecast = mutableStateOf(emptyWeatherForecast)
    val weatherForecast by _weatherForecast

    private var userLocation: UserLocation? = null

    lateinit var temperatureUnit : TemperatureUnit
    lateinit var appTheme : AppTheme

    private val db = Room.databaseBuilder(
        application,
        WeatherAppDatabase::class.java, DB_NAME
    ).build()

    private val repository = WeatherAppRepository(db)

    private val sharedPreferences = SharedPreferencesUtil(application)
    private val apiResponseParser = WeatherApiResponseBodyParserImpl(application)

    init {
        loadConfiguration()
    }

    fun loadConfiguration() {
        appTheme = sharedPreferences.getTheme()
        userLocation = sharedPreferences.getLocation()
        temperatureUnit = sharedPreferences.getTemperatureUnit()

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

        sharedPreferences.saveTheme(appTheme)

        applyTheme(appTheme)
    }

    fun useTemperatureUnit(unit: TemperatureUnit) {
        if (unit == temperatureUnit) return

        temperatureUnit = if (temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }

        sharedPreferences.saveTemperatureUnit(temperatureUnit)
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
        sharedPreferences.saveLocation(userLocation!!)
        Log.i(TAG, "updateUserLocation: $userLocation")
    }

    fun updateForecast(onError: (e: Exception) -> Unit) {
        if (userLocation == null) {
            Log.e(TAG, "updateForecast: No user location")
            throw IllegalStateException("No user location provided")
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "updateForecast: getting forecast")
            val response = try {
                RetrofitInstance.api.getForecast(API_KEY, userLocation!!.toString())
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
}