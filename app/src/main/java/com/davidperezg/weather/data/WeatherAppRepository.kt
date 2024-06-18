package com.davidperezg.weather.data

import android.os.Parcel
import android.os.Parcelable

class WeatherAppRepository(
    private val db: WeatherAppDatabase
) {
    private inline fun <reified T : Parcelable> getParcelableCreator(): Parcelable.Creator<T> =
        T::class.java.getDeclaredField("CREATOR").get(null) as Parcelable.Creator<T>


    suspend fun getWeatherForecast(): WeatherForecast? {
        // Get the WeatherForecastEntity from the database
        val weatherForecastEntity = db
            .weatherForecastDao()
            .getWeatherForecast() ?: return null

        val parcel = Parcel.obtain()
        parcel.unmarshall(weatherForecastEntity.data, 0, weatherForecastEntity.data.size)
        parcel.setDataPosition(0)
        val forecast = getParcelableCreator<WeatherForecast>().createFromParcel(parcel)
        parcel.recycle()
        return forecast
    }

    suspend fun insertOrUpdateWeatherForecast(weatherForecast: WeatherForecast) {
        // Transform the WeatherForecast object to a WeatherForecastEntity
        val parcel = Parcel.obtain()
        weatherForecast.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        val weatherForecastEntity = WeatherForecastEntity(data = bytes)
        db.weatherForecastDao().insertOrUpdateWeatherForecast(weatherForecastEntity)
    }
}
