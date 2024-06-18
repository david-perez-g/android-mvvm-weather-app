package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.davidperezg.weather.WeatherViewModel

@Composable
fun WeekForecast(vm: WeatherViewModel) {
    Column(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium
            )
            .fillMaxWidth()
            .wrapContentHeight()

    ) {
        for (day in vm.weatherForecast.days) {
            DayResume(day)
        }
    }
}