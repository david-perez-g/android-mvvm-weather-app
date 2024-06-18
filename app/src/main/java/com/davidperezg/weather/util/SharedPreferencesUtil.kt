package com.davidperezg.weather.util

import android.app.Application
import android.content.Context
import com.davidperezg.weather.data.TemperatureUnit
import com.davidperezg.weather.data.AppTheme
import com.davidperezg.weather.data.UserLocation

class SharedPreferencesUtil(
    application: Application
) {
    companion object  {
        const val SHARED_PREFERENCES_NAME = "WeatherApp"
        const val TEMPERATURE_UNIT = "TEMPERATURE_UNIT"
        const val APP_THEME = "APP_THEME"
        const val LAST_LOCATION = "LAST_LOCATION"
        const val DEFAULT_LAST_LOCATION = "0,0"
    }

    private val preferences =
        application.getSharedPreferences(
            SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )


    fun getTemperatureUnit(): TemperatureUnit {
        return TemperatureUnit.valueOf(
            preferences.getString(
                TEMPERATURE_UNIT, TemperatureUnit.CELSIUS.name
            )!!
        )
    }

    fun saveTemperatureUnit(unit: TemperatureUnit) {
        preferences.edit().putString(TEMPERATURE_UNIT, unit.name).apply()
    }

    fun getTheme(): AppTheme {
        return AppTheme.valueOf(
            preferences.getString(
                APP_THEME, AppTheme.LIGHT.name
            )!!
        )
    }

    fun saveTheme(theme: AppTheme) {
        preferences.edit().putString(APP_THEME, theme.name).apply()
    }

    fun getLocation(): UserLocation? {
        val location = preferences.getString(LAST_LOCATION, DEFAULT_LAST_LOCATION)!!
        if (location == DEFAULT_LAST_LOCATION) {
            return null
        }
        return UserLocation.fromString(location)
    }

    fun saveLocation(location: UserLocation) {
        preferences.edit().putString(LAST_LOCATION, location.toString()).apply()
    }
}