package com.example.climatrack.services

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.climatrack.R
import com.example.climatrack.activities.DashboardActivity
import com.example.climatrack.utils.FirebaseHelper
import com.google.android.gms.location.*
import com.google.firebase.firestore.SetOptions

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userId: String = ""
    private var activeOrderNum: String? = null

    companion object {
        const val CHANNEL_ID = "LocationTrackingChannel"
        const val NOTIFICATION_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_UPDATE_ORDER = "ACTION_UPDATE_ORDER"
        const val ACTION_STOP_ORDER = "ACTION_STOP_ORDER"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val EXTRA_ORDER_NUM = "EXTRA_ORDER_NUM"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationInFirestore(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
                val newOrderNum = intent.getStringExtra(EXTRA_ORDER_NUM)
                if (newOrderNum != null) activeOrderNum = newOrderNum
                startTracking()
            }
            ACTION_UPDATE_ORDER -> {
                activeOrderNum = intent.getStringExtra(EXTRA_ORDER_NUM)
            }
            ACTION_STOP_ORDER -> {
                activeOrderNum = null
            }
            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        val notification = createNotification("Seguimiento de ubicación activo")
        startForeground(NOTIFICATION_ID, notification)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30000)
            .setMinUpdateIntervalMillis(15000)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun updateLocationInFirestore(lat: Double, lon: Double) {
        android.util.Log.d("TRACKING_SVC", "Actualizando ubicación: $lat, $lon para usuario: $userId")
        if (userId.isNotEmpty()) {
            val userUpdate = mapOf(
                "lastLat" to lat,
                "lastLon" to lon,
                "isActive" to 1,
                "timestamp" to System.currentTimeMillis()
            )
            FirebaseHelper.db.collection("usuarios").document(userId)
                .set(userUpdate, SetOptions.merge())
                .addOnSuccessListener { android.util.Log.d("TRACKING_SVC", "Firestore Usuario OK") }
                .addOnFailureListener { e -> android.util.Log.e("TRACKING_SVC", "Firestore Usuario Error: ${e.message}") }
        }

        activeOrderNum?.let { orderNum ->
            android.util.Log.d("TRACKING_SVC", "Actualizando orden: $orderNum")
            val orderUpdate = mapOf(
                "tecnicoLat" to lat,
                "tecnicoLon" to lon
            )
            FirebaseHelper.db.collection("ordenes").document(orderNum)
                .set(orderUpdate, SetOptions.merge())
                .addOnSuccessListener { android.util.Log.d("TRACKING_SVC", "Firestore Orden OK") }
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ClimaTrack en curso")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_nav_home)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Seguimiento de Ubicación",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
