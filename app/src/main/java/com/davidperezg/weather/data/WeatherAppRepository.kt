package com.davidperezg.weather.data

import android.os.Parcel
import android.os.Parcelable

interface WeatherAppRepository {
    suspend fun getWeatherForecast(): WeatherForecast?
    suspend fun insertOrUpdateWeatherForecast(weatherForecast: WeatherForecast)
}

class WeatherAppRepositoryImpl(
    private val weatherForecastDao: WeatherForecastDao
): WeatherAppRepository {
    private inline fun <reified T : Parcelable> getParcelableCreator(): Parcelable.Creator<T> =
        T::class.java.getDeclaredField("CREATOR").get(null) as Parcelable.Creator<T>

    override suspend fun getWeatherForecast(): WeatherForecast? {
        val weatherForecastEntity =
            weatherForecastDao.getWeatherForecast() ?: return null

        val parcel = Parcel.obtain()
        parcel.unmarshall(weatherForecastEntity.data, 0, weatherForecastEntity.data.size)
        parcel.setDataPosition(0)
        val forecast = getParcelableCreator<WeatherForecast>().createFromParcel(parcel)
        parcel.recycle()
        return forecast
    }

    override suspend fun insertOrUpdateWeatherForecast(weatherForecast: WeatherForecast) {
        val parcel = Parcel.obtain()
        weatherForecast.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        val weatherForecastEntity = WeatherForecastEntity(data = bytes)
        weatherForecastDao.insertOrUpdateWeatherForecast(weatherForecastEntity)
    }
}
