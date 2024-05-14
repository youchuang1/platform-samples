package com.example.platform.location.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

@Composable
fun GeofenceBroadcastReceiver(
    systemAction: String,
    systemEvent: (userActivity: String) -> Unit,
) {
    val TAG = "GeofenceReceiver"  // 定义日志标签
    val context = LocalContext.current  // 获取当前 Compose 上下文
    val currentSystemOnEvent by rememberUpdatedState(systemEvent)  // 使用 rememberUpdatedState 来确保事件处理始终是最新的

    DisposableEffect(context, systemAction) {  // 使用 DisposableEffect 来管理资源的生命周期
        val intentFilter = IntentFilter(systemAction)  // 创建意图过滤器，用于监听指定的系统动作
        val broadcast = object : BroadcastReceiver() {  // 定义广播接收器
            override fun onReceive(context: Context?, intent: Intent?) {  // 处理接收到的广播
                val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) } ?: return  // 从意图中获取地理围栏事件

                if (geofencingEvent.hasError()) {  // 检查地理围栏事件是否有错误
                    val errorMessage =
                        GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)  // 获取错误消息
                    Log.e(TAG, "onReceive: $errorMessage")  // 记录错误
                    return
                }
                val alertString = "地理围栏警报 :" +
                        " 触发 ${geofencingEvent.triggeringGeofences}" +
                        " 过渡 ${geofencingEvent.geofenceTransition}"
                Log.d(
                    TAG,
                    alertString
                )
                currentSystemOnEvent(alertString)  // 处理地理围栏警报事件
            }
        }
        context.registerReceiver(broadcast, intentFilter)  // 注册广播接收器
        onDispose {
            context.unregisterReceiver(broadcast)  // 注销广播接收器
        }
    }
}
