package com.example.platform.location.locationupdates

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.platform.base.PermissionBox
import com.google.android.catalog.framework.annotations.Sample
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.TimeUnit


@SuppressLint("MissingPermission")
@Sample(
    name = "位置 - 更新",
    description = "本示例展示如何获取位置更新",
    documentation = "https://developer.android.com/training/location/request-updates",
)
@Composable
fun LocationUpdatesScreen() {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    // 至少需要粗略位置权限
    PermissionBox(
        permissions = permissions,
        requiredPermissions = listOf(permissions.first()),
    ) {
        LocationUpdatesContent(
            usePreciseLocation = it.contains(Manifest.permission.ACCESS_FINE_LOCATION),
        )
    }
}

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesContent(usePreciseLocation: Boolean) {
    // 定义位置请求以定义位置更新的频率和精确度
    var locationRequest by remember {
        mutableStateOf<LocationRequest?>(null)
    }
    // 用于跟踪接收到的位置更新
    var locationUpdates by remember {
        mutableStateOf("")
    }

    // 当位置请求有效时，注册位置更新的效果
    if (locationRequest != null) {
        LocationUpdatesEffect(locationRequest!!) { result ->
            // 更新显示的位置信息
            for (currentLocation in result.locations) {
                locationUpdates = "${System.currentTimeMillis()}:\n" +
                        "- @lat: ${currentLocation.latitude}\n" +
                        "- @lng: ${currentLocation.longitude}\n" +
                        "- Accuracy: ${currentLocation.accuracy}\n\n" +
                        locationUpdates
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            // 开关按钮，用于开始和停止位置更新
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "启用位置更新")
                Spacer(modifier = Modifier.padding(8.dp))
                Switch(
                    checked = locationRequest != null,
                    onCheckedChange = { checked ->
                        locationRequest = if (checked) {
                            // 根据需要和授予的权限确定精确度
                            val priority = if (usePreciseLocation) {
                                Priority.PRIORITY_HIGH_ACCURACY
                            } else {
                                Priority.PRIORITY_BALANCED_POWER_ACCURACY
                            }
                            LocationRequest.Builder(priority, TimeUnit.SECONDS.toMillis(3)).build()
                        } else {
                            null
                        }
                    },
                )
            }
        }
        item {
            Text(text = locationUpdates)
        }
    }
}

/**
 * 一个效果，根据提供的请求请求位置更新，并确保当组件进入或退出组合时添加和移除更新。
 */
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesEffect(
    locationRequest: LocationRequest,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onUpdate: (result: LocationResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnUpdate by rememberUpdatedState(newValue = onUpdate)

    // 参数更改时重新启动效果
    DisposableEffect(locationRequest, lifecycleOwner) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentOnUpdate(result)
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper(),
                )
            } else if (event == Lifecycle.Event.ON_STOP) {
                locationClient.removeLocationUpdates(locationCallback)
            }
        }

        // 将观察者添加到生命周期
        lifecycleOwner.lifecycle.addObserver(observer)

        // 组合离开时，移除观察者
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}