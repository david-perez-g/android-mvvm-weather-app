package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidperezg.weather.data.WeatherForecast

@Composable
fun HourForecast(forecast: State<WeatherForecast>) {
    LazyRow(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium
            )
            .height(90.dp)
            .fillMaxWidth()
    ) {
        items(forecast.value.following24Hours) { hour ->
            HourResume(hour)
        }
    }
}