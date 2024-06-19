package com.davidperezg.weather.data

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.davidperezg.weather.util.WeekDay
import com.davidperezg.weather.util.toCelsius
import com.davidperezg.weather.util.toFahrenheit
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

enum class TemperatureUnit {
    CELSIUS, FAHRENHEIT
}

enum class AppTheme {
    LIGHT, DARK
}


@Entity
data class WeatherForecastEntity(
    @PrimaryKey val id: Int = 0, // constant ID
    val data: ByteArray
)

@Parcelize
data class WeatherForecast(
    val city: String,
    val country: String,
    val currentState: WeatherCurrentState,
    val days: List<WeatherDayResume>,
    val today: WeatherDayResume,
    val following24Hours: List<WeatherHourResume>,
) : Parcelable

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String {
        return "$latitude,$longitude"
    }

    companion object {
        fun fromString(string: String): UserLocation {
            val pieces = string.split(",")
            return UserLocation(
                pieces.first().toDouble(),
                pieces.last().toDouble()
            )
        }
    }
}

@Parcelize
class Temperature(
    // value to be parceled
    private var _value: Double,
    private var temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
) : Parcelable {
    @IgnoredOnParcel
    private var value by mutableStateOf(_value) // value to be observed in the UI

    fun useTemperatureUnit(unit: TemperatureUnit) {
        // no need to change anything
        if (unit == temperatureUnit) return
        switchTemperatureUnit()
    }

    private fun switchTemperatureUnit() {
        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperatureUnit = TemperatureUnit.CELSIUS
            value = toCelsius(value)
            _value = value
        } else {
            temperatureUnit = TemperatureUnit.FAHRENHEIT
            value = toFahrenheit(value)
            _value = value
        }
    }

    override fun toString(): String {
        val unit = if (temperatureUnit == TemperatureUnit.CELSIUS) "C" else "F"
        return "${String.format("%.1f", value)}˚$unit"
    }
}

@Parcelize
data class WeatherCondition(
    val text: String,
    val imageResourceId: Int,
) : Parcelable

@Parcelize
data class WeatherHourResume(
    val date: Date,
    val condition: WeatherCondition,
    val temperature: Temperature,
    val willItRain: Boolean,
    val chanceOfRain: Int,
) : Parcelable

@Parcelize
data class WeatherDayResume(
    val date: Date,
    val weekDay: WeekDay,
    val condition: WeatherCondition,
    val minimumTemperature: Temperature,
    val maximumTemperature: Temperature,
    val hours: List<WeatherHourResume>,
) : Parcelable

@Parcelize
data class WeatherCurrentState(
    val date: Date,
    val temperature: Temperature,
    val temperatureFeelsLike: Temperature,
    val condition: WeatherCondition,
    val humidity: Int,
) : Parcelable
