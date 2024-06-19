package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidperezg.weather.R
import com.davidperezg.weather.data.WeatherForecast

@Composable
fun CurrentWeatherState(forecast: State<WeatherForecast>) {
    Row {
        Text(
            text = "${forecast.value.city}, ${forecast.value.country}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.width(3.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "location icon"
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row {
            Text(
                text = forecast.value.currentState.temperature.toString(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row {
            Text(
                text =
                "${forecast.value.currentState.condition.text}, " +
                        "${forecast.value.today.minimumTemperature} / ${forecast.value.today.maximumTemperature}, \n"
                        + stringResource(R.string.temp_feels_like) + " ${forecast.value.currentState.temperatureFeelsLike}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
