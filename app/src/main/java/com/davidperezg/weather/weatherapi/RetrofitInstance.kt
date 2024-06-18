package com.davidperezg.weather.weatherapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}