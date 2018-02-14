package me.felixguo.vibrato

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlin.math.max
import kotlin.math.roundToInt


private const val LOCATION_INTERVAL = 1000L
private const val LOCATION_DISTANCE = 10F
private const val TAG = "VIBRATO-SERVICE"
private const val NOTIFICATION_CHANNEL_ID = "vibrato-on-your-way-channel"

class LocationService : Service() {
    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private var vibrator: Vibrator? = null
    private var locationListeners = arrayOf(
        LocationListener(LocationManager.GPS_PROVIDER),
        LocationListener(LocationManager.NETWORK_PROVIDER)
    )

    private var destination = LatLng(0.0, 0.0)
    private var radius: Double = 0.0

    private var results = FloatArray(1)

    private lateinit var notification: Notification.Builder
    private val handler = Handler()

    private inner class LocationListener(val provider: String) : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            locationChanged(location, provider)
        }

        override fun onProviderDisabled(provider: String) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }
    }

    fun locationChanged(location: Location, provider: String) {
        Location.distanceBetween(
            destination.latitude, destination.longitude,
            location.latitude, location.longitude, results
        )
        val distanceAway = results[0].roundToInt()
        val distanceFromVibration = max(0, (distanceAway - radius).roundToInt())
        val notificationContent = "You are ${distanceAway}m away from your destination! ${distanceFromVibration}m before vibration!"
        notification.setContentText(notificationContent)
        notification.setStyle(Notification.BigTextStyle().bigText(notificationContent))
        updateNotification()
        Log.d(TAG, "$provider: ${results[0] < radius}")
        if (results[0] < radius) {
            val vibrateOnce = object : Runnable {
                override fun run() {
                    vibrator?.vibrate(500)
                    handler.postDelayed(this, 500)
                }
            }
            handler.post(vibrateOnce)
            locationListeners.forEach { locationListeners.forEach { locationManager.removeUpdates(it) } }
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        destination = LatLng(
            intent.getDoubleExtra(INTENT_DESTINATION_LAT, 0.0),
            intent.getDoubleExtra(INTENT_DESTINATION_LNG, 0.0)
        )
        radius = intent.getDoubleExtra(INTENT_RADIUS, 0.0)
        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        locationManager =
                applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            locationListeners.forEach {
                locationManager.requestLocationUpdates(
                    it.provider,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    it
                )
            }
        } catch (ex: SecurityException) {

        } catch (ex: IllegalArgumentException) {

        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        setupNotifications()
        updateNotification()
    }

    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "On Your Way Notification",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        notification = Notification.Builder(this)
            .setContentTitle("You're on your way!")
            .setContentText("Waiting for location update...")
            .setStyle(Notification.BigTextStyle()
                .bigText("Waiting for location update..."))
            .setSmallIcon(R.drawable.ic_pin)
            .setColor(resources.getColor(R.color.colorPrimaryDark))
            .setContentIntent(pIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setChannelId(NOTIFICATION_CHANNEL_ID)
                }
            }
    }

    private fun updateNotification() {
        notificationManager.notify(0, notification.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        notificationManager.cancel(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        notificationManager.cancel(0)
        vibrator?.cancel()
    }
}
