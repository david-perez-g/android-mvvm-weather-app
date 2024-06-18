package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidperezg.weather.data.WeatherHourResume
import com.davidperezg.weather.util.formatHourFromDate

@Composable
fun HourResume(hour: WeatherHourResume) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatHourFromDate(hour.date),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()

        ) {
            Image(
                painterResource(id = hour.condition.imageResourceId),
                contentDescription = hour.condition.text,
                modifier = Modifier
                    .size(30.dp)
                    .fillMaxWidth()
            )

        }

        Text(
            text = hour.temperature.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}