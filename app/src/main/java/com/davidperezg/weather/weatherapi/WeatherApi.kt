package com.davidperezg.weather.weatherapi

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") daysInForecast: Int = 7,
        @Query("aqi") airQualityIndex: String = "no",
        @Query("alerts") meteorologicalAlerts: String = "no",
    ): Response<ApiWeatherForecast>
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