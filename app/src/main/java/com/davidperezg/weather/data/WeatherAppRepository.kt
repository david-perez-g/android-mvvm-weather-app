package com.davidperezg.weather.data

import android.util.Log
import com.davidperezg.weather.util.parcelForecast
import com.davidperezg.weather.util.unParcelForecastEntity
import com.davidperezg.weather.weatherapi.WeatherApi
import com.davidperezg.weather.weatherapi.WeatherApiResponseBodyParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface WeatherAppRepository {
    fun getWeatherForecast(): Flow<WeatherForecast>
    suspend fun insertOrUpdateWeatherForecast(weatherForecast: WeatherForecast)
    suspend fun fetchForecast(
        apiKey: String,
        location: UserLocation,
    )

    suspend fun setTemperatureUnit(unit: TemperatureUnit)
}

class WeatherAppRepositoryImpl(
    private val weatherForecastDao: WeatherForecastDao,
    private val weatherApi: WeatherApi,
    private val apiResponseParser: WeatherApiResponseBodyParser,
) : WeatherAppRepository {

    private val TAG: String = "WeatherAppRepository"

    private var latestWeatherForecastEntity: WeatherForecastEntity? = null

    private val forecastEntityFlow = weatherForecastDao
        .getWeatherForecast()
        .onEach {
            latestWeatherForecastEntity = it
        }

    override fun getWeatherForecast(): Flow<WeatherForecast> {
        return forecastEntityFlow.map { forecastEntity ->
            forecastEntity?.let {
                return@map unParcelForecastEntity(it.data)
            } ?: defaultWeatherForecast
        }
    }


    override suspend fun insertOrUpdateWeatherForecast(weatherForecast: WeatherForecast) {
        weatherForecastDao.insertOrUpdate(
            WeatherForecastEntity(
                data = parcelForecast(weatherForecast)
            )
        )
    }

    override suspend fun fetchForecast(
        apiKey: String,
        location: UserLocation,
    ) {
        val response = weatherApi.getForecast(apiKey, location.toString())

        if (response.isSuccessful && response.body() != null) {
            Log.i(TAG, "updateForecast: got a successful response!")
            val forecast = apiResponseParser.parse(response.body()!!)
            insertOrUpdateWeatherForecast(forecast)
        }
    }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        apiResponseParser.useTemperatureUnit(unit)

        if (latestWeatherForecastEntity == null) {
            return
        }

        val forecast = unParcelForecastEntity(latestWeatherForecastEntity!!.data)

        forecast.apply {
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
            CoroutineScope(Dispatchers.IO).launch {
                insertOrUpdateWeatherForecast(forecast)
            }
        }
    }
}
