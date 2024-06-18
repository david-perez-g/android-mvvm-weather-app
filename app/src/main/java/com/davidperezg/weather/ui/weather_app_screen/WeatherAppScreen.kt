package com.davidperezg.weather.ui.weather_app_screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davidperezg.weather.R
import com.davidperezg.weather.WeatherViewModel
import com.davidperezg.weather.util.Routes
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppScreen(
    context: Context,
    viewModel: WeatherViewModel,
    onLocationPermissionsNeeded: () -> Unit,
    onNavigate: (route: String) -> Unit,
) {
    val isRefreshing = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WeatherApp",
                        style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        onNavigate(Routes.SETTINGS)
                    }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        },
    ) { paddingValues ->

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing.value),
            onRefresh = {
                try {
                    Toast.makeText(
                        context,
                        R.string.updating,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    viewModel.updateForecast(onError = {
                        Toast.makeText(
                            context,
                            R.string.unable_to_fetch_weather_data,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    })
                } catch (e: IllegalStateException) {
                    onLocationPermissionsNeeded()
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = 10.dp
                    )
                    .padding(horizontal = 14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    thickness = 3.dp
                )

                Spacer(modifier = Modifier.height(15.dp))
                CurrentWeatherState(viewModel)
                Spacer(modifier = Modifier.height(15.dp))
                HourForecast(viewModel)
                Spacer(modifier = Modifier.height(15.dp))
                WeekForecast(viewModel)
            }
        }
    }

}