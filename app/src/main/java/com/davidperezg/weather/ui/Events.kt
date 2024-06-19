package com.davidperezg.weather.ui

import com.davidperezg.weather.data.AppTheme
import com.davidperezg.weather.data.TemperatureUnit

// Events send from the view model to UI
sealed class ViewModelEvent {
    data class ShowSnackbar(val message: String, val action: String? = null) : ViewModelEvent()
    data class Navigate(val route: String) : ViewModelEvent()
}

// Events send from the UI to the view model
sealed class UiEvent {
    object UpdateForecast : UiEvent()
    object SettingsIconClick : UiEvent()
    data class SetTemperatureUnit(val unit: TemperatureUnit) : UiEvent()
    data class SetAppTheme(val theme: AppTheme) : UiEvent()
}
