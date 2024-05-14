package com.example.platform.location.geofencing

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.example.platform.base.PermissionBox
import com.example.platform.location.utils.CUSTOM_INTENT_GEOFENCE
import com.google.android.catalog.framework.annotations.Sample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("InlinedApi")
@Sample(
    name = "位置 - 创建和监控地理围栏",
    description = "本示例展示创建和监控地理围栏的最佳实践",
    documentation = "https://developer.android.com/training/location/geofencing",
)
@Composable
fun GeofencingScreen() {
    val permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
    // 至少需要粗略位置权限
    PermissionBox(
        permissions = permissions,
        requiredPermissions = listOf(permissions.first()),
    ) {
        // 从 Android 10 开始，需要后台位置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionBox(
                permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            ) {
                GeofencingControls()
            }
        } else {
            GeofencingControls()
        }
    }
}

@Composable
private fun GeofencingControls() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geofenceManager = remember { GeofenceManager(context) }
    var geofenceTransitionEventInfo by remember {
        mutableStateOf("")
    }

    // 注册和解注册地理围栏使用的 DisposableEffect
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                geofenceManager.deregisterGeofence()
            }
        }
    }

    // 注册本地广播接收地理围栏的活动转换更新
    GeofenceBroadcastReceiver(systemAction = CUSTOM_INTENT_GEOFENCE) { event ->
        geofenceTransitionEventInfo = event
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GeofenceList(geofenceManager)
        Button(
            onClick = {
                if (geofenceManager.geofenceList.isNotEmpty()) {
                    geofenceManager.registerGeofence()
                } else {
                    Toast.makeText(
                        context,
                        "请至少添加一个地理围栏以进行监控",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        ) {
            Text(text = "注册地理围栏")
        }

        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    geofenceManager.deregisterGeofence()
                }
            },
        ) {
            Text(text = "解注册地理围栏")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = geofenceTransitionEventInfo)
    }
}

@Composable
fun GeofenceList(geofenceManager: GeofenceManager) {
    // 管理地理围栏的复选框状态
    val checkedGeoFence1 = remember { mutableStateOf(false) }
    val checkedGeoFence2 = remember { mutableStateOf(false) }
    val checkedGeoFence3 = remember { mutableStateOf(false) }

    Text(text = "可用的地理围栏")
    Row(
        Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checkedGeoFence1.value,
            onCheckedChange = { checked ->
                if (checked) {
                    geofenceManager.addGeofence(
                        "statue_of_liberty",
                        location = Location("").apply {
                            latitude = 40.689403968838015
                            longitude = -74.04453795094359
                        },
                    )
                } else {
                    geofenceManager.removeGeofence("statue_of_libery")
                }
                checkedGeoFence1.value = checked
            },
        )
        Text(text = "自由女神像")
    }
    Row(
        Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checkedGeoFence2.value,
            onCheckedChange = { checked ->
                if (checked) {
                    geofenceManager.addGeofence(
                        "eiffel_tower",
                        location = Location("").apply {
                            latitude = 48.85850
                            longitude = 2.29455
                        },
                    )
                } else {
                    geofenceManager.removeGeofence("eiffel_tower")
                }
                checkedGeoFence2.value = checked
            },
        )
        Text(text = "艾菲尔铁塔")
    }
    Row(
        Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checkedGeoFence3.value,
            onCheckedChange = { checked ->
                if (checked) {
                    geofenceManager.addGeofence(
                        "vatican_city",
                        location = Location("").apply {
                            latitude = 41.90238
                            longitude = 12.45398
                        },
                    )
                } else {
                    geofenceManager.removeGeofence("vatican_city")
                }
                checkedGeoFence3.value = checked
            },
        )
        Text(text = "梵蒂冈城")
    }
}
