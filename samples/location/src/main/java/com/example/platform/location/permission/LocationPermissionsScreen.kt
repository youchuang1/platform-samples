package com.example.platform.location.permission

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.catalog.framework.annotations.Sample

@RequiresApi(Build.VERSION_CODES.Q)
@Sample(
    name = "位置 - 权限",
    description = "本示例展示获取位置权限的最佳实践",
    documentation = "https://developer.android.com/training/location/permissions",
    tags = ["permissions"],
)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionScreen() {
    val context = LocalContext.current

    // 通常情况下，粗略位置权限已足够使用
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    // 当精确位置很重要时，请求两个权限，但需处理用户只授权粗略位置的情况
    val fineLocationPermissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
        ),
    )

    // 在极少数情况下，可能需要访问后台位置
    val bgLocationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    )

    // 跟踪权限请求的解释对话框状态
    var rationaleState by remember {
        mutableStateOf<RationaleState?>(null)
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 当需要时显示解释对话框
            rationaleState?.run { PermissionRationaleDialog(rationaleState = this) }

            PermissionRequestButton(
                isGranted = locationPermissionState.status.isGranted,
                title = "粗略位置访问权限",
            ) {
                if (locationPermissionState.status.shouldShowRationale) {
                    rationaleState = RationaleState(
                        "请求粗略位置访问权限",
                        "为使用此功能，请授予位置权限对话框的访问权限。\n\n您想继续吗？",
                    ) { proceed ->
                        if (proceed) {
                            locationPermissionState.launchPermissionRequest()
                        }
                        rationaleState = null
                    }
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            }

            PermissionRequestButton(
                isGranted = fineLocationPermissionState.allPermissionsGranted,
                title = "精确位置访问权限",
            ) {
                if (fineLocationPermissionState.shouldShowRationale) {
                    rationaleState = RationaleState(
                        "请求精确位置",
                        "为使用此功能，请授予位置权限对话框的访问权限。\n\n您想继续吗？",
                    ) { proceed ->
                        if (proceed) {
                            fineLocationPermissionState.launchMultiplePermissionRequest()
                        }
                        rationaleState = null
                    }
                } else {
                    fineLocationPermissionState.launchMultiplePermissionRequest()
                }
            }

            // 从 Android Q 开始，背景位置访问需要单独的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionRequestButton(
                    isGranted = bgLocationPermissionState.status.isGranted,
                    title = "后台位置访问权限",
                ) {
                    if (locationPermissionState.status.isGranted || fineLocationPermissionState.allPermissionsGranted) {
                        if (bgLocationPermissionState.status.shouldShowRationale) {
                            rationaleState = RationaleState(
                                "请求后台位置",
                                "为使用此功能，请授予后台位置权限对话框的访问权限。\n\n您想继续吗？",
                            ) { proceed ->
                                if (proceed) {
                                    bgLocationPermissionState.launchPermissionRequest()
                                }
                                rationaleState = null
                            }
                        } else {
                            bgLocationPermissionState.launchPermissionRequest()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "请先授权粗略或精确位置访问权限",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = { context.startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS)) },
        ) {
            Icon(Icons.Outlined.Settings, "位置设置")
        }
    }
}
