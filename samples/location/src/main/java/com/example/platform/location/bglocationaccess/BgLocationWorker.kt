package com.example.platform.location.bglocationaccess

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// 用于后台位置更新的 Worker 类
class BgLocationWorker(context: Context, param: WorkerParameters) :
    CoroutineWorker(context, param) {
    companion object {
        // 工作的唯一名称
        val workName = "BgLocationWorker"
        private const val TAG = "BackgroundLocationWork"
    }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    // 重写 doWork 方法以执行后台位置获取任务
    override suspend fun doWork(): Result {
        // 检查定位权限
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，返回失败结果
            return Result.failure()
        }
        // 请求当前位置信息
        locationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            // 当成功获取位置时打印位置数据
            location?.let {
                Log.d(
                    TAG,
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
            }
        }
        // 如果任务顺利执行完成，返回成功结果
        return Result.success()
    }
}
