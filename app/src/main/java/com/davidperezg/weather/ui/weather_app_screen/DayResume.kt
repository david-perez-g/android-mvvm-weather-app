package com.davidperezg.weather.ui.weather_app_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.davidperezg.weather.data.WeatherDayResume
import com.davidperezg.weather.util.formatDayFromDate


@Composable
fun DayResume(day: WeatherDayResume) {
    Row(
        modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDayFromDate(day.date),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.15f)
        )

        Text(
            text = stringResource(day.weekDay.getStringResourceId()),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.30f)
        )

        Image(
            painter = painterResource(id = day.condition.imageResourceId),
            contentDescription = day.condition.text,
            modifier = Modifier
                .weight(0.10f)
                .align(Alignment.CenterVertically)
        )

        Text(
            text = "${day.minimumTemperature} / ${day.maximumTemperature}",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(0.35f)
        )
    }
}

