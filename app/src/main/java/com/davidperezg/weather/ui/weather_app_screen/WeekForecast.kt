package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.davidperezg.weather.data.WeatherForecast

@Composable
fun WeekForecast(forecast: State<WeatherForecast>) {
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        for (day in forecast.value.days) {
            DayResume(day)
        }
    }
}