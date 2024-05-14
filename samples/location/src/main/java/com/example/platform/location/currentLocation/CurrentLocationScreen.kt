package com.example.platform.location.currentLocation

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.platform.base.PermissionBox
import com.google.android.catalog.framework.annotations.Sample
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Sample(
    name = "位置 - 获取当前位置",
    description = "本示例展示如何请求当前位置",
    documentation = "https://developer.android.com/training/location/retrieve-current",
)
@Composable
fun CurrentLocationScreen() {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    PermissionBox(
        permissions = permissions,
        requiredPermissions = listOf(permissions.first()),
        onGranted = {
            CurrentLocationContent(
                usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
            )
        },
    )
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun CurrentLocationContent(usePreciseLocation: Boolean) {
    // rememberCoroutineScope 创建一个可以在当前组合范围内启动协程的作用域
    val scope = rememberCoroutineScope()
    // 获取当前上下文
    val context = LocalContext.current
    // 创建一个位置客户端，用于访问设备的位置信息
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    // 使用 remember 和 mutableStateOf 存储位置信息，确保当位置信息更新时界面会重新组合
    var locationInfo by remember {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxWidth() // 设置列的宽度填充父容器的最大宽度
            .animateContentSize() // 为列添加动画效果
            .padding(16.dp), // 设置列的内边距
        verticalArrangement = Arrangement.spacedBy(8.dp), // 垂直方向上子项之间的间隔
        horizontalAlignment = Alignment.CenterHorizontally, // 子项在水平方向上居中对齐
    ) {
        Button(
            onClick = {
                // 获取最后已知位置较快并且可以节省电池使用，但这些信息可能是过时的。
                // 位置可能为空，因为之前没有客户端访问过位置信息，
                // 或者设备设置中关闭了位置信息。
                // 请处理位置为空的情况，并且在使用该方法之前可以添加额外的检查。
                scope.launch(Dispatchers.IO) {
                    val result = locationClient.lastLocation.await()
                    // 检查 result 是否为空，并更新位置信息
                    locationInfo = if (result == null) {
                        "没有已知的最后位置。请尝试首先获取当前位置"
                    } else {
                        "当前位置是\n" + "纬度 : ${result.latitude}\n" +
                                "经度 : ${result.longitude}\n" + "获取时间 ${System.currentTimeMillis()}" +
                                "\n水平精度 : ${result.accuracy}\n" + "自设备启动以来经过的时间 : ${result.elapsedRealtimeNanos}\n" +
                                "海拔 : ${result.altitude}\n + vAcc ： ${result.verticalAccuracyMeters}\n" +
                                "vel : ${result.speed}\n" +
                                "sAcc : ${result.speedAccuracyMetersPerSecond}\n"
                    }
                    Log.d("CurrentLocationScreen", "Last known location: $result")
                }
            },
        ) {
            Text("获取最后已知位置")
        }

        Button(
            onClick = {
                // 如果需要更精确或更新的设备位置，请使用此方法
                scope.launch(Dispatchers.IO) {
                    // 根据是否需要精确位置设置优先级
                    val priority = if (usePreciseLocation) {
                        Priority.PRIORITY_HIGH_ACCURACY
                    } else {
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY
                    }
                    // 获取当前位置，传入设定的优先级和取消令牌
                    val result = locationClient.getCurrentLocation(
                        priority,
                        CancellationTokenSource().token,
                    ).await()
                    // 更新位置信息
                    result?.let { fetchedLocation ->
                        locationInfo =
                            "当前位置是\n" + "纬度 : ${fetchedLocation.latitude}\n" +
                                    "经度 : ${fetchedLocation.longitude}\n" + "获取时间 ${System.currentTimeMillis()}" +
                                    "\n水平精度 : ${fetchedLocation.accuracy}\n" +
                                    "自设备启动以来经过的时间 : ${fetchedLocation.elapsedRealtimeNanos}\n" +
                                    "海拔 : ${fetchedLocation.altitude}\n + vAcc ： ${fetchedLocation.verticalAccuracyMeters}\n" +
                                    "vel : ${fetchedLocation.speed}\n" +
                                    "sAcc : ${fetchedLocation.speedAccuracyMetersPerSecond}\n"
                    }
                    Log.d("CurrentLocationScreen", "Current location: $result")
                }
            },
        ) {
            Text(text = "获取当前位置")
        }
        // 显示位置信息
        Text(
            text = locationInfo,
        )
    }
}

