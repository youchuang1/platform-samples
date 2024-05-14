/*
 * 版权所有 2023 The Android Open Source Project
 *
 * 根据 Apache 许可证 2.0 版（“许可证”）授权，除非遵守许可证，否则你不得使用此文件。
 * 你可以在以下网址获得许可证副本：
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非适用法律要求或书面同意，按许可证分发的软件按“原样”分发，
 * 不附带任何明示或暗示的担保或条件。请参阅许可证了解管理权限和限制的具体语言。
 */

package com.example.platform.ui.windowmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.area.WindowAreaCapability
import androidx.window.area.WindowAreaController
import androidx.window.area.WindowAreaInfo
import androidx.window.area.WindowAreaPresentationSessionCallback
import androidx.window.area.WindowAreaSessionPresenter
import androidx.window.core.ExperimentalWindowApi
import com.example.platform.ui.windowmanager.databinding.ActivityDualScreenBinding
import com.example.platform.ui.windowmanager.infolog.InfoLogAdapter
import com.example.platform.ui.windowmanager.util.getCurrentTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

@OptIn(ExperimentalWindowApi::class)
class DualScreenActivity : AppCompatActivity(), WindowAreaPresentationSessionCallback {

    private lateinit var binding: ActivityDualScreenBinding
    private val infoLogAdapter = InfoLogAdapter()

    private lateinit var windowAreaController: WindowAreaController
    private lateinit var displayExecutor: Executor
    private var windowAreaSession: WindowAreaSessionPresenter? = null
    private var windowAreaInfo: WindowAreaInfo? = null
    private var capabilityStatus: WindowAreaCapability.Status =
        WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED

    private val presentOperation = WindowAreaCapability.Operation.OPERATION_PRESENT_ON_AREA
    private val logTag = "ConcurrentDisplays"

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityDualScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.adapter = infoLogAdapter

        displayExecutor = ContextCompat.getMainExecutor(this)
        windowAreaController = WindowAreaController.getOrCreate()

        binding.button.setOnClickListener {
            toggleDualScreenMode()
        }

        updateCapabilities()
    }

    private fun toggleDualScreenMode() {
        //这里我们没有检查状态，因为 b/302183399
        if (windowAreaSession != null) {
            windowAreaSession?.close()
        } else {
            windowAreaInfo?.token?.let { token ->
                windowAreaController.presentContentOnWindowArea(
                    token = token,
                    activity = this,
                    executor = displayExecutor,
                    windowAreaPresentationSessionCallback = this
                )
            }
        }
    }

    private fun updateCapabilities() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                windowAreaController.windowAreaInfos
                    .map { info -> info.firstOrNull { it.type == WindowAreaInfo.Type.TYPE_REAR_FACING } }
                    .onEach { info -> windowAreaInfo = info }
                    .map { it?.getCapability(presentOperation)?.status ?: WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED }
                    .distinctUntilChanged()
                    .collect {
                        capabilityStatus = it
                        updateUI()
                        infoLogAdapter.notifyDataSetChanged()
                    }
            }
        }
    }

    private fun updateUI() {
        if (windowAreaSession != null) {
            binding.button.isEnabled = true
            binding.status.text = "禁用双屏模式"
        } else {
            when (capabilityStatus) {
                WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNSUPPORTED -> {
                    binding.button.isEnabled = false
                    binding.status.text = "此设备不支持双屏"
                }
                WindowAreaCapability.Status.WINDOW_AREA_STATUS_UNAVAILABLE -> {
                    binding.button.isEnabled = false
                    binding.status.text = "双屏当前不可用"
                }
                WindowAreaCapability.Status.WINDOW_AREA_STATUS_AVAILABLE -> {
                    binding.button.isEnabled = true
                    binding.status.text = "启用双屏模式"
                }
                else -> {
                    binding.button.isEnabled = false
                    binding.status.text = "双屏状态未知"
                }
            }
        }
    }

    override fun onSessionStarted(session: WindowAreaSessionPresenter) {
        infoLogAdapter.append(getCurrentTimeString(), "演示会话已开始")
        windowAreaSession = session
        val view = TextView(session.context)
        view.text = "你好世界，从另一个屏幕问候！"
        session.setContentView(view)
        updateUI()
    }

    override fun onSessionEnded(t: Throwable?) {
        if (t != null) {
            Log.e(logTag, "出现错误: ${t.message}")
        }
        infoLogAdapter.append(getCurrentTimeString(), "演示会话已结束")
        windowAreaSession = null
    }

    override fun onContainerVisibilityChanged(isVisible: Boolean) {
        infoLogAdapter.append(getCurrentTimeString(), "演示内容是否可见: $isVisible")
    }
}
