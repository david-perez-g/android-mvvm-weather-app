package com.davidperezg.weather

import android.app.Application
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Date

interface WeatherAPI {
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") daysInForecast: Int = 7,
        @Query("aqi") airQualityIndex: String = "no",
        @Query("alerts") meteorologicalAlerts: String = "no",
    ): Response<ApiWeatherForecast>
}


object RetrofitInstance {
    val api: WeatherAPI by lazy {
        Retrofit.Builder()
            .baseUrl("http://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherAPI::class.java)
    }
}


class ApiResponseBodyParser(
    private val application: Application,
    private var temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
) {
    private val weatherConditionCodes = listOf(
        listOf(1000) to "clear",
        listOf(1003) to "partly_cloudy",
        listOf(1006) to "cloudy",
        listOf(1009) to "overcast",
        listOf(1030, 1135, 1147) to "mist",
        listOf(1066, 1069, 1072, 1114, 1117, 1213, 1219, 1222, 1225, 1237, 1278) to "snow",
        listOf(1063, 1186, 1189, 1195, 1198, 1201, 1204, 1207, 1243, 1246, 1255) to "rain",
        listOf(1150, 1153, 1168, 1180, 1183, 1240, 1249, 1252, 1258, 1261, 1264) to "shower_rain",
        listOf(1087, 1273, 1276, 1279, 1282) to "thunderstorm",
    )

    fun useTemperatureUnit(newTemperatureUnit: TemperatureUnit) {
        temperatureUnit = newTemperatureUnit
    }

    fun parse(responseBody: ApiWeatherForecast): WeatherForecast {
        val forecastDays = getDays(responseBody.forecast.forecastday)
        val currentTime = getDate(responseBody.current.last_updated_epoch)

        return WeatherForecast(
            city = responseBody.location.name,
            country = responseBody.location.country,
            currentState = WeatherCurrentState(
                date = currentTime,
                temperature = getTemperature(responseBody.current.temp_c),
                temperatureFeelsLike = getTemperature(responseBody.current.feelslike_c),
                condition = getWeatherCondition(
                    responseBody.current.condition.code,
                    responseBody.current.is_day == 1
                ),
                humidity = responseBody.current.humidity
            ),
            days = forecastDays,
            today = forecastDays.first(),
            following24Hours = getFollowing24Hours(currentTime, forecastDays),
        )
    }

    private fun getTemperature(temperatureCelsius: Double): Temperature {
        return when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> Temperature(temperatureCelsius)
            TemperatureUnit.FAHRENHEIT -> Temperature(
                toFahrenheit(temperatureCelsius), TemperatureUnit.FAHRENHEIT
            )
        }
    }

    private fun getFollowing24Hours(
        currentTime: Date,
        days: List<WeatherDayResume>,
    ): List<WeatherHourResume> {
        val hours = mutableListOf<WeatherHourResume>()
        val currentHour = getHourFromDate(currentTime)
        val today = days.first()
        val tomorrow = days[1]

        for (hour in today.hours) {
            if (getHourFromDate(hour.date) >= currentHour) {
                hours.add(hour)
            }
        }

        for (hour in tomorrow.hours) {
            if (getHourFromDate(hour.date) < currentHour) {
                hours.add(hour)
            }
        }

        return hours
    }

    private fun getDays(apiDays: List<ApiForecastDay>): List<WeatherDayResume> {
        val days = mutableListOf<WeatherDayResume>()

        for (apiDay in apiDays) {
            val date = getDate(apiDay.date_epoch)
            days.add(
                WeatherDayResume(
                    date = date,
                    condition = getWeatherCondition(apiDay.day.condition.code, isDay = true),
                    maximumTemperature = getTemperature(apiDay.day.maxtemp_c),
                    minimumTemperature = getTemperature(apiDay.day.mintemp_c),
                    weekDay = getWeekDayFromDate(date),
                    hours = getDayHours(apiDay.hour)
                )
            )
        }

        return days
    }

    private fun getDate(epoch: Long): Date {
        return Date(epoch * 1000L)
    }

    // Gets the drawable icon for the corresponding weather condition
    private fun getWeatherConditionImage(code: Int, isDay: Boolean): Int {
        for (condition in weatherConditionCodes) {
            val (codes, text) = condition
            if (code in codes) {
                val timeOfDay = if (isDay) "_day" else "_night"

                return application.resources.getIdentifier(
                    "$text$timeOfDay", "drawable", application.packageName
                )
            }
        }

        // not found
        return 0
    }

    private fun getWeatherCondition(code: Int, isDay: Boolean): WeatherCondition {
        val resourceId = application.resources.getIdentifier(
            "weather_condition_day$code", "string", application.packageName
        )

        val descriptionText = if (resourceId == 0) {
            "Unknown"
        } else {
            application.resources.getString(resourceId)
        }

        val imageResourceId = getWeatherConditionImage(code, isDay)

        return WeatherCondition(
            descriptionText,
            imageResourceId
        )
    }

    private fun getDayHours(apiHours: List<ApiHourResume>): List<WeatherHourResume> {
        val hours = mutableListOf<WeatherHourResume>()

        for (apiHour in apiHours) {
            val hourDate = getDate(apiHour.time_epoch)

            hours.add(
                WeatherHourResume(
                    date = hourDate,
                    condition = getWeatherCondition(apiHour.condition.code, apiHour.is_day == 1),
                    temperature = getTemperature(apiHour.temp_c),
                    willItRain = apiHour.will_it_rain == 1,
                    chanceOfRain = apiHour.chance_of_rain
                )
            )
        }

        hours.sortBy { getHourFromDate(it.date) }

        return hours
    }
}

data class ApiWeatherForecast(
    val location: ApiLocation, val current: ApiCurrentWeather, val forecast: ApiForecast,
)

data class ApiLocation(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Int,
    val localtime: String,
)

data class ApiCurrentWeather(
    val last_updated_epoch: Long,
    val temp_c: Double,
    val is_day: Int,
    val condition: ApiCondition,
    val humidity: Int,
    val feelslike_c: Double,
)

data class ApiCondition(
    val code: Int,
)

data class ApiForecast(
    val forecastday: List<ApiForecastDay>,
)

data class ApiForecastDay(
    val date_epoch: Long,
    val day: ApiDayResume,
    val astro: Astro,
    val hour: List<ApiHourResume>,
)

data class ApiDayResume(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_will_it_rain: Int,
    val daily_chance_of_rain: Int,
    val condition: ApiCondition,
)

data class Astro(
    val sunrise: String,
    val sunset: String,
)

data class ApiHourResume(
    val time_epoch: Long,
    val temp_c: Double,
    val is_day: Int,
    val will_it_rain: Int,
    val chance_of_rain: Int,
    val condition: ApiCondition,
)