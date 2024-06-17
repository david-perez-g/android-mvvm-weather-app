package com.davidperezg.weather

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