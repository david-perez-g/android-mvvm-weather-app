package com.davidperezg.weather

import android.app.Application
import com.davidperezg.weather.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class WeatherAppApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WeatherAppApplication)
            modules(appModule)
        }
    }
}