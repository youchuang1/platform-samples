package com.example.platform.location.currentLocation

import android.Manifest
import android.annotation.SuppressLint
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationInfo by remember {
        mutableStateOf("")
    }

    Column(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = {
                // 获取最后已知位置较快并且可以节省电池使用，但这些信息可能是过时的。
                // 位置可能为空，因为之前没有客户端访问过位置信息，
                // 或者设备设置中关闭了位置信息。
                // 请处理位置为空的情况，并且在使用该方法之前可以添加额外的检查。
                scope.launch(Dispatchers.IO) {
                    val result = locationClient.lastLocation.await()
                    locationInfo = if (result == null) {
                        "没有已知的最后位置。请尝试首先获取当前位置"
                    } else {
                        "当前位置是\n" + "纬度 : ${result.latitude}\n" +
                                "经度 : ${result.longitude}\n" + "获取时间 ${System.currentTimeMillis()}"
                    }
                }
            },
        ) {
            Text("获取最后已知位置")
        }

        Button(
            onClick = {
                // 如果需要更精确或更新的设备位置，请使用此方法
                scope.launch(Dispatchers.IO) {
                    val priority = if (usePreciseLocation) {
                        Priority.PRIORITY_HIGH_ACCURACY
                    } else {
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY
                    }
                    val result = locationClient.getCurrentLocation(
                        priority,
                        CancellationTokenSource().token,
                    ).await()
                    result?.let { fetchedLocation ->
                        locationInfo =
                            "当前位置是\n" + "纬度 : ${fetchedLocation.latitude}\n" +
                                    "经度 : ${fetchedLocation.longitude}\n" + "获取时间 ${System.currentTimeMillis()}"
                    }
                }
            },
        ) {
            Text(text = "获取当前位置")
        }
        Text(
            text = locationInfo,
        )
    }
}
