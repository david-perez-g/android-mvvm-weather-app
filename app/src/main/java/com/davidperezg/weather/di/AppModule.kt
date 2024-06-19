package com.davidperezg.weather.di

import android.app.Application
import androidx.room.Room
import com.davidperezg.weather.WeatherViewModel
import com.davidperezg.weather.data.WeatherAppDatabase
import com.davidperezg.weather.data.WeatherAppRepositoryImpl
import com.davidperezg.weather.util.SharedPreferencesUtil
import com.davidperezg.weather.weatherapi.WeatherApi
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

    single {
        WeatherApiResponseBodyParserImpl(get())
    }

    single {
        Room.databaseBuilder(
            get<Application>(),
            WeatherAppDatabase::class.java,
            "WeatherAppDatabase"
        ).build()
    }

    single {
        WeatherAppRepositoryImpl(get<WeatherAppDatabase>().weatherForecastDao())
    }

    single {
        SharedPreferencesUtil(get())
    }

    viewModel {
        WeatherViewModel(
            repository = get<WeatherAppRepositoryImpl>(),
            spUtil = get(),
            weatherApi = get(),
            apiResponseParser = get<WeatherApiResponseBodyParserImpl>(),
        )
    }
}