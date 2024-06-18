package com.davidperezg.weather

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.davidperezg.weather.data.Temperature
import com.davidperezg.weather.data.TemperatureUnit
import com.davidperezg.weather.data.UITheme
import com.davidperezg.weather.data.UserLocation
import com.davidperezg.weather.data.WeatherAppDatabase
import com.davidperezg.weather.data.WeatherAppRepository
import com.davidperezg.weather.data.WeatherCondition
import com.davidperezg.weather.data.WeatherCurrentState
import com.davidperezg.weather.data.WeatherDayResume
import com.davidperezg.weather.data.WeatherForecast
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
        const val SHARED_PREFERENCES_NAME = "WeatherApp"
        const val DB_NAME = "WeatherAppDatabase"
        const val SP_TEMPERATURE_UNIT = "TEMPERATURE_UNIT"
        const val SP_UI_THEME = "UI_THEME"
        const val SP_LAST_LOCATION = "LAST_LOCATION"
        const val DEFAULT_LOCATION = "0,0"
        private const val API_KEY = "66e299409de74d0ca49151039241306" // WeatherAPI.com
    }

    private val _weatherForecast = mutableStateOf(emptyWeatherForecast)
    val weatherForecast by _weatherForecast

    private var userLocation: UserLocation? = null

    lateinit var temperatureUnit : TemperatureUnit
    lateinit var uiTheme : UITheme

    private val db = Room.databaseBuilder(
        application,
        WeatherAppDatabase::class.java, DB_NAME
    ).build()

    private val repository = WeatherAppRepository(db)

    private val sharedPreferences = application
        .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val responseBodyParser = WeatherApiResponseBodyParserImpl(application)

    init {
        loadConfiguration()
    }

    fun loadConfiguration() {
        val savedTemperatureUnit = sharedPreferences.getString(SP_TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name)
        temperatureUnit = TemperatureUnit.valueOf(savedTemperatureUnit!!)
        responseBodyParser.useTemperatureUnit(temperatureUnit)

        val savedThemeMode = sharedPreferences.getString(SP_UI_THEME, UITheme.LIGHT.name)
        uiTheme = UITheme.valueOf(savedThemeMode!!)

        val savedLocation = sharedPreferences.getString(SP_LAST_LOCATION, DEFAULT_LOCATION)
        userLocation = if (savedLocation!! == DEFAULT_LOCATION) {
            null
        } else {
            UserLocation.fromString(savedLocation)
        }

        viewModelScope.launch {
            _weatherForecast.value = repository.getWeatherForecast() ?: emptyWeatherForecast
            applyTemperatureUnit(temperatureUnit)
        }


        applyTheme(uiTheme)
    }

    fun switchUITheme() {
        uiTheme = if (uiTheme == UITheme.LIGHT) {
            UITheme.DARK
        } else {
            UITheme.LIGHT
        }

        sharedPreferences.edit().putString(SP_UI_THEME, uiTheme.name).apply()

        applyTheme(uiTheme)
    }

    fun useTemperatureUnit(unit: TemperatureUnit) {
        if (unit == temperatureUnit) return

        temperatureUnit = if (temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }

        sharedPreferences.edit().putString(SP_TEMPERATURE_UNIT, temperatureUnit.name).apply()
        responseBodyParser.useTemperatureUnit(temperatureUnit)
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

    private fun applyTheme(theme: UITheme) {
        when (theme) {
            UITheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            UITheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    fun updateUserLocation(location: UserLocation) {
        userLocation = location
        sharedPreferences.edit().putString(SP_LAST_LOCATION, userLocation.toString()).apply()
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
                _weatherForecast.value = responseBodyParser.parse(response.body()!!)
                repository.insertOrUpdateWeatherForecast(weatherForecast)
            }
        }
    }
}