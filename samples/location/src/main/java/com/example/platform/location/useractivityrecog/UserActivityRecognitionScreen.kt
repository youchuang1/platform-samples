package com.example.platform.location.useractivityrecog

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.example.platform.base.PermissionBox
import com.example.platform.location.utils.CUSTOM_INTENT_USER_ACTION
import com.google.android.catalog.framework.annotations.Sample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Sample(
    name = "位置 - 用户活动识别",
    description = "本示例展示如何检测用户活动，如行走、驾驶等。",
    documentation = "https://developer.android.com/training/location/transitions",
)
@Composable
fun UserActivityRecognitionScreen() {
    val activityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Manifest.permission.ACTIVITY_RECOGNITION
    } else {
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
    }

    PermissionBox(permissions = listOf(activityPermission)) {
        UserActivityRecognitionContent()
    }
}

@SuppressLint("InlinedApi")
@RequiresPermission(
    anyOf = [
        Manifest.permission.ACTIVITY_RECOGNITION,
        "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
    ],
)
@Composable
fun UserActivityRecognitionContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember {
        UserActivityTransitionManager(context)
    }
    var currentUserActivity by remember {
        mutableStateOf("Unknown")
    }

    // 确保在组件销毁时取消注册活动转换
    DisposableEffect(LocalLifecycleOwner.current) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                manager.deregisterActivityTransitions()
            }
        }
    }

    // 注册本地广播以接收活动转换更新
    UserActivityBroadcastReceiver(systemAction = CUSTOM_INTENT_USER_ACTION) { userActivity ->
        currentUserActivity = userActivity
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    manager.registerActivityTransitions()
                }
            },
        ) {
            Text(text = "注册活动转换更新")
        }
        Button(
            onClick = {
                currentUserActivity = ""
                scope.launch(Dispatchers.IO) {
                    manager.deregisterActivityTransitions()
                }
            },
        ) {
            Text(text = "取消注册活动转换更新")
        }
        if (currentUserActivity.isNotBlank()) {
            Text(
                text = "当前活动为 = $currentUserActivity",
            )
        }
    }
}
