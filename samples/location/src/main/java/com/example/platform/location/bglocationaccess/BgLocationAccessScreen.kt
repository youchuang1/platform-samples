package com.example.platform.location.bglocationaccess

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.platform.base.PermissionBox
import com.google.android.catalog.framework.annotations.Sample
import java.util.concurrent.TimeUnit


@Sample(
    name = "位置 - 后台位置更新",
    description = "此示例演示如何在应用位于后台时访问位置并获取位置更新",
    documentation = "https://developer.android.com/training/location/background",
)
@Composable
fun BgLocationAccessScreen() {
    // Request for foreground permissions first
    PermissionBox(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        requiredPermissions = listOf(Manifest.permission.ACCESS_COARSE_LOCATION),
        onGranted = {
            // From Android 10 onwards request for background permission only after fine or coarse is granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionBox(permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    BackgroundLocationControls()
                }
            } else {
                BackgroundLocationControls()
            }
        },
    )
}

@Composable
private fun BackgroundLocationControls() {
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    // Component UI state holder
    data class ControlsState(val text: String, val action: String, val onClick: () -> Unit)

    // Observe the worker state to show enable/disable UI
    val workerState by workManager.getWorkInfosForUniqueWorkLiveData(BgLocationWorker.workName)
        .observeAsState()

    val controlsState = remember(workerState) {
        // Find if there is any enqueued or running worker and provide UI state
        val enqueued = workerState?.find { !it.state.isFinished } != null
        if (enqueued) {
            ControlsState(
                text = "每 15 分钟检查一次 logcat 以获取位置更新",
                action = "禁用更新",
                onClick = {
                    workManager.cancelUniqueWork(BgLocationWorker.workName)
                },
            )
        } else {
            ControlsState(
                text = "启用位置更新并将应用置于后台。",
                action = "启用更新",
                onClick = {
                    // Schedule a periodic worker to check for location every 15 min
                    workManager.enqueueUniquePeriodicWork(
                        BgLocationWorker.workName,
                        ExistingPeriodicWorkPolicy.KEEP,
                        PeriodicWorkRequestBuilder<BgLocationWorker>(
                            15,
                            TimeUnit.MINUTES,
                        ).build(),
                    )
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = controlsState.text)
        Button(onClick = controlsState.onClick) {
            Text(text = controlsState.action)
        }
    }
}
