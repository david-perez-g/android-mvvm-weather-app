package com.davidperezg.weather.util

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager

interface LocationReceiver {
    fun startReceivingUpdates()
    fun subscribe(listener: (location: Location) -> Unit)
}

class LocationReceiverImpl(
    application: Application
): LocationReceiver {
    private var locationManager: LocationManager = application.getSystemService(
        Context.LOCATION_SERVICE
    ) as LocationManager

    private var subscriber: (location: Location) -> Unit = {}

    private var locationListener: LocationListener = LocationListener {
        subscriber(it)
    }


    @Suppress("MissingPermission")
    override fun startReceivingUpdates() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0L,
            0f,
            locationListener
        )
    }

    override fun subscribe(listener: (location: Location) -> Unit) {
        this.subscriber = listener
    }
}
