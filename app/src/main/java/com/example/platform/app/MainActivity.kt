package com.example.platform.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.google.android.catalog.framework.ui.CatalogActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

/**
 * MainApp 是应用程序的主类，负责初始化和配置应用的全局状态。
 * 此类实现了 ImageLoaderFactory 接口，用于创建 ImageLoader 实例。
 * 使用 @HiltAndroidApp 注解，Hilt 会自动生成并管理应用的依赖注入组件。
 *
 * 参考 [casa-android](https://github.com/google/casa-android#create-catalog-app) 设置。
 */
@HiltAndroidApp
class MainApp : Application(), ImageLoaderFactory {

    /**
     * 创建并配置 Coil 的 ImageLoader 实例。
     * 这个实例添加了 VideoFrameDecoder.Factory，用于处理视频帧的解码。
     * 开启了 crossfade 动画效果。
     *
     * @return 配置好的 ImageLoader 实例
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}

/**
 * MainActivity 是应用程序的入口点，使用 [CatalogActivity] 展示平台样本目录。
 * 使用 @AndroidEntryPoint 注解，Hilt 会自动注入依赖。
 */
@AndroidEntryPoint
class MainActivity : CatalogActivity()
