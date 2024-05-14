package com.example.platform.location.geofencing

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import com.example.platform.location.utils.CUSTOM_INTENT_GEOFENCE
import com.example.platform.location.utils.CUSTOM_REQUEST_CODE_GEOFENCE
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class GeofenceManager(context: Context) {
    private val TAG = "GeofenceManager"  // 日志标签
    private val client = LocationServices.getGeofencingClient(context)  // 地理围栏客户端
    val geofenceList = mutableMapOf<String, Geofence>()  // 保存地理围栏的列表

    // 创建 Pending Intent 用于触发地理围栏事件
    private val geofencingPendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            Intent(CUSTOM_INTENT_GEOFENCE),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    // 添加地理围栏
    fun addGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float = 100.0f,
        expirationTimeInMillis: Long = 30 * 60 * 1000,  // 30分钟
    ) {
        geofenceList[key] = createGeofence(key, location, radiusInMeters, expirationTimeInMillis)
    }

    // 移除地理围栏
    fun removeGeofence(key: String) {
        geofenceList.remove(key)
    }

    // 注册地理围栏
    @SuppressLint("MissingPermission")
    fun registerGeofence() {
        client.addGeofences(createGeofencingRequest(), geofencingPendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                Log.d(TAG, "registerGeofence: Failure\n$exception")
            }
    }

    // 注销地理围栏
    suspend fun deregisterGeofence() = kotlin.runCatching {
        client.removeGeofences(geofencingPendingIntent).await()
        geofenceList.clear()
    }

    // 创建地理围栏请求
    private fun createGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GEOFENCE_TRANSITION_ENTER)  // 设置初始触发条件为进入
            addGeofences(geofenceList.values.toList())
        }.build()
    }

    // 创建单个地理围栏
    private fun createGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float,
        expirationTimeInMillis: Long,
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)  // 设置请求 ID
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters)  // 设置圆形区域
            .setExpirationDuration(expirationTimeInMillis)  // 设置过期时间
            .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)  // 设置触发条件（进入或退出）
            .build()
    }

}
