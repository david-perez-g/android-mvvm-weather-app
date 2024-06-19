package com.davidperezg.weather.data

import android.app.Application
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.davidperezg.weather.util.WeekDay
import com.davidperezg.weather.util.parcelForecast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date


val defaultWeatherForecast = WeatherForecast(
    "-", "-", WeatherCurrentState(
        date = Date(0),
        temperature = Temperature(0.0),
        temperatureFeelsLike = Temperature(0.0),
        condition = WeatherCondition("-", 0),
        humidity = 50,
    ),
    days = listOf(),
    today = WeatherDayResume(
        date = Date(0),
        condition = WeatherCondition("-", 0),
        hours = listOf(),
        minimumTemperature = Temperature(0.0),
        maximumTemperature = Temperature(0.0),
        weekDay = WeekDay.FRIDAY,
    ), following24Hours = listOf()
)

@Dao
interface WeatherForecastDao {
    @Query("SELECT * FROM WeatherForecastEntity WHERE id = 0")
    fun getWeatherForecast(): Flow<WeatherForecastEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(weatherForecast: WeatherForecastEntity)
}

@Database(
    entities = [
        WeatherForecastEntity::class
    ],
    version = 1
)
abstract class WeatherAppDatabase
    : RoomDatabase() {
    abstract fun weatherForecastDao(): WeatherForecastDao

    companion object {
        @Volatile private var INSTANCE: WeatherAppDatabase? = null

        fun getInstance(app: Application): WeatherAppDatabase {
            INSTANCE?.let {
                return INSTANCE!!
            }

            return buildDatabase(app).also { INSTANCE = it }
        }

        private fun buildDatabase(app: Application) =
            Room.databaseBuilder(
                app,
                WeatherAppDatabase::class.java,
                "WeatherAppDatabase",
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        CoroutineScope(Dispatchers.IO).launch {
                            prePopulateDatabase(getInstance(app).weatherForecastDao())
                        }
                    }
                })
                .build()

        suspend fun prePopulateDatabase(weatherForecastDao: WeatherForecastDao) {
            Log.i("WeatherDatabase", "prePopulateDatabase: populating DB")
            weatherForecastDao.insertOrUpdate(
                WeatherForecastEntity(
                    data = parcelForecast(defaultWeatherForecast)
                )
            )
        }
    }
}
