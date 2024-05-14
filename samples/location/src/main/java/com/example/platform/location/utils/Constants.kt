package com.example.platform.location.utils

// 用于位置示例的常量定义

const val CUSTOM_INTENT_USER_ACTION = "USER-ACTIVITY-DETECTION-INTENT-ACTION"
// 自定义的 Intent 动作，用于用户活动识别广播接收器的过滤器

const val CUSTOM_REQUEST_CODE_USER_ACTION = 1000
// 用户活动识别请求代码，用于区分不同的 PendingIntent 请求

const val CUSTOM_INTENT_GEOFENCE = "GEOFENCE-TRANSITION-INTENT-ACTION"
// 自定义的 Intent 动作，用于地理围栏转换事件的广播接收器过滤器

const val CUSTOM_REQUEST_CODE_GEOFENCE = 1001
// 地理围栏转换请求代码，用于区分不同的 PendingIntent 请求
