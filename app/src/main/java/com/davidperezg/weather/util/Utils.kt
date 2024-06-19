package com.davidperezg.weather.util

import android.os.Parcel
import android.os.Parcelable
import com.davidperezg.weather.R
import com.davidperezg.weather.data.WeatherForecast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun toFahrenheit(celsiusDegrees: Double): Double {
    return 1.8 * celsiusDegrees + 32
}

fun toCelsius(fahrenheitDegrees: Double): Double {
    return (fahrenheitDegrees - 32) / 1.8
}


enum class WeekDay {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;

    fun getStringResourceId(): Int {
        return when (valueOf(name)) {
            SUNDAY -> R.string.week_day_sunday
            MONDAY -> R.string.week_day_monday
            TUESDAY -> R.string.week_day_tuesday
            WEDNESDAY -> R.string.week_day_wednesday
            THURSDAY -> R.string.week_day_thursday
            FRIDAY -> R.string.week_day_friday
            SATURDAY -> R.string.week_day_saturday
        }
    }
}


fun formatDayFromDate(date: Date): String {
    val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("GMT")
    return sdf.format(date)
}

fun formatHourFromDate(date: Date): String {
    val sdf = SimpleDateFormat("HH:00", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(date)
}

fun getWeekDayFromDate(date: Date): WeekDay {
    val calendar = Calendar.getInstance()
    calendar.timeZone = TimeZone.getTimeZone("GMT")
    calendar.time = date
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    return WeekDay.values()[dayOfWeek - 1]
}

fun getHourFromDate(date: Date): Int {
    val hour = formatHourFromDate(date)
    return hour.substring(0, 2).toInt()
}

inline fun <reified T : Parcelable> getParcelableCreator(): Parcelable.Creator<T> =
    T::class.java.getDeclaredField("CREATOR").get(null) as Parcelable.Creator<T>



fun unParcelForecastEntity(bytes: ByteArray): WeatherForecast {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    val forecast = getParcelableCreator<WeatherForecast>().createFromParcel(parcel)
    parcel.recycle()
    return forecast
}

fun parcelForecast(forecast: WeatherForecast): ByteArray {
    val parcel = Parcel.obtain()
    forecast.writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}