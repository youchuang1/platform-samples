package com.example.platform.location.useractivityrecog

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
import com.example.platform.location.useractivityrecog.UserActivityTransitionManager.Companion.getActivityType
import com.example.platform.location.useractivityrecog.UserActivityTransitionManager.Companion.getTransitionType
import com.google.android.gms.location.ActivityTransitionResult

@Composable
fun UserActivityBroadcastReceiver(
    systemAction: String,
    systemEvent: (userActivity: String) -> Unit,
) {
    val context = LocalContext.current
    val currentSystemOnEvent by rememberUpdatedState(systemEvent)

    // 使用 DisposableEffect 来注册和注销广播接收器
    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // 从 Intent 中提取用户活动转换结果
                val result = intent?.let { ActivityTransitionResult.extractResult(it) } ?: return
                var resultStr = ""
                // 循环遍历所有活动事件，并构建字符串描述
                for (event in result.transitionEvents) {
                    resultStr += "${getActivityType(event.activityType)} " +
                            "- ${getTransitionType(event.transitionType)}\n"
                }
                Log.d("UserActivityReceiver", "onReceive: $resultStr")
                currentSystemOnEvent(resultStr)
            }
        }
        context.registerReceiver(broadcast, intentFilter)
        // 组件离开时注销广播接收器
        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}
