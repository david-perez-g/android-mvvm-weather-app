package com.davidperezg.weather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidperezg.weather.ui.theme.WeatherTheme

class SettingsActivity : AppCompatActivity() {
    private val weatherViewModel by viewModels<WeatherViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherTheme {
                SettingsScreen(viewModel = weatherViewModel, { finish() })
            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WeatherViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(
            top = padding.calculateTopPadding(),
            start = 14.dp,
            end = 14.dp
        )) {
            Divider(
                color = MaterialTheme.colorScheme.inverseOnSurface,
                thickness = 3.dp
            )

            Row(
                Modifier.height(45.dp)
            ) {
                Text(
                    text = stringResource(R.string.dark_mode),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(CenterVertically)
                )

                var isDarkMode by remember { mutableStateOf(viewModel.uiTheme == UITheme.DARK) }

                Spacer(Modifier.weight(1f))

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isChecked ->
                        isDarkMode = isChecked
                        viewModel.switchUITheme()
                    },
                    modifier = Modifier.align(CenterVertically)
                )
            }
            Row(
                Modifier.height(45.dp)
            ) {
                Text(
                    text = stringResource(R.string.temperature_unit),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(CenterVertically)
                )

                Spacer(Modifier.weight(1f))

                var temperatureUnit by remember { mutableStateOf(viewModel.temperatureUnit) }
                var expanded by remember { mutableStateOf(false) }

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(if (temperatureUnit == TemperatureUnit.FAHRENHEIT) "Fahrenheit" else "Celsius")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Fahrenheit")
                            },
                            onClick = {
                            temperatureUnit = TemperatureUnit.FAHRENHEIT
                            viewModel.useTemperatureUnit(temperatureUnit)
                            expanded = false
                        })

                        DropdownMenuItem(
                            text = {
                                Text("Celsius")
                            },
                            onClick = {
                            temperatureUnit = TemperatureUnit.CELSIUS
                            viewModel.useTemperatureUnit(temperatureUnit)
                            expanded = false
                        })
                    }
                }

            }
        }
    }
}
