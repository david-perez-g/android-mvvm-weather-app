package com.davidperezg.weather.di

import com.davidperezg.weather.WeatherViewModel
import com.davidperezg.weather.data.WeatherAppDatabase
import com.davidperezg.weather.data.WeatherAppRepository
import com.davidperezg.weather.data.WeatherAppRepositoryImpl
import com.davidperezg.weather.util.LocationReceiver
import com.davidperezg.weather.util.LocationReceiverImpl
import com.davidperezg.weather.util.SharedPreferencesUtil
import com.davidperezg.weather.weatherapi.WeatherApi
import com.davidperezg.weather.weatherapi.WeatherApiResponseBodyParser
import com.davidperezg.weather.weatherapi.WeatherApiResponseBodyParserImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("http://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    single<WeatherApiResponseBodyParser> {
        WeatherApiResponseBodyParserImpl(get())
    }

    single {
        WeatherAppDatabase.getInstance(get())
    }

    single<WeatherAppRepository> {
        WeatherAppRepositoryImpl(
            weatherForecastDao = get<WeatherAppDatabase>().weatherForecastDao(),
            weatherApi = get(),
            apiResponseParser = get()
        )
    }

    single {
        SharedPreferencesUtil(get())
    }

    single<LocationReceiver> {
        LocationReceiverImpl(get())
    }

    viewModel {
        WeatherViewModel(
            context = get(),
            repository = get(),
            spUtil = get(),
            locationReceiver = get()
        )
    }
}