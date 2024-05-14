package com.example.platform.location.useractivityrecog

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.example.platform.location.utils.CUSTOM_INTENT_USER_ACTION
import com.example.platform.location.utils.CUSTOM_REQUEST_CODE_USER_ACTION
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.tasks.await

class UserActivityTransitionManager(context: Context) {

    companion object {
        // 根据活动类型的整数值返回相应的字符串描述
        fun getActivityType(int: Int): String {
            return when (int) {
                0 -> "IN_VEHICLE"
                1 -> "ON_BICYCLE"
                2 -> "ON_FOOT"
                3 -> "STILL"
                4 -> "UNKNOWN"
                5 -> "TILTING"
                7 -> "WALKING"
                8 -> "RUNNING"
                else -> "UNKNOWN"
            }
        }

        // 根据转换类型的整数值返回相应的字符串描述
        fun getTransitionType(int: Int): String {
            return when (int) {
                0 -> "STARTED"
                1 -> "STOPPED"
                else -> ""
            }
        }
    }

    // 初始化要监控的活动转换列表
    private val activityTransitions: List<ActivityTransition> by lazy {
        listOf(
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_ENTER
            ),
            getUserActivity(
                DetectedActivity.RUNNING, ActivityTransition.ACTIVITY_TRANSITION_EXIT
            ),
        )
    }

    private val activityClient = ActivityRecognition.getClient(context) // 获取 ActivityRecognition 的客户端实例

    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_USER_ACTION,
            Intent(CUSTOM_INTENT_USER_ACTION),
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                PendingIntent.FLAG_CANCEL_CURRENT
            } else {
                PendingIntent.FLAG_MUTABLE
            }
        )
    }

    // 构建活动转换对象
    private fun getUserActivity(detectedActivity: Int, transitionType: Int): ActivityTransition {
        return ActivityTransition.Builder().setActivityType(detectedActivity)
            .setActivityTransition(transitionType).build()
    }

    // 注册活动转换更新
    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        ]
    )
    suspend fun registerActivityTransitions() = kotlin.runCatching {
        activityClient.requestActivityTransitionUpdates(
            ActivityTransitionRequest(activityTransitions), pendingIntent
        ).await()
    }

    // 注销活动转换更新
    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        ]
    )
    suspend fun deregisterActivityTransitions() = kotlin.runCatching {
        activityClient.removeActivityUpdates(pendingIntent).await()
    }
}
