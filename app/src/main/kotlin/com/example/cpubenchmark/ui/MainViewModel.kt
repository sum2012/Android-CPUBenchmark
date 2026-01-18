package com.example.cpubenchmark.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cpubenchmark.data.BenchmarkResult
import com.example.cpubenchmark.data.TestState
import com.example.cpubenchmark.data.ThreadType
import com.example.cpubenchmark.logic.BenchmarkEngine
import com.example.cpubenchmark.util.DeviceInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 主界面的ViewModel
 * 管理測試狀態和業務邏輯
 */
class MainViewModel : ViewModel() {

    // UI狀態
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 測試工作
    private var currentJob: Job? = null

    init {
        // 初始化設備信息
        _uiState.update { state ->
            state.copy(
                deviceName = DeviceInfo.getDeviceModel(),
                cpuCores = DeviceInfo.getCpuCoreCount(),
                cpuType = DeviceInfo.getEstimatedCpuInfo()
            )
        }
    }

    /**
     * 開始單線程測試
     */
    fun startSingleThreadTest() {
        // 取消之前的測試
        currentJob?.cancel()

        _uiState.update { state ->
            state.copy(
                testState = TestState.Running(0f, 0),
                currentProgress = 0f,
                currentCore = 0
            )
        }

        currentJob = viewModelScope.launch {
            try {
                val result = BenchmarkEngine.runSingleThreadTest { progress ->
                    _uiState.update { state ->
                        state.copy(
                            currentProgress = progress,
                            testState = TestState.Running(progress, 1)
                        )
                    }
                }

                // 更新結果
                _uiState.update { state ->
                    state.copy(
                        singleThreadResult = result,
                        testState = TestState.Completed(result),
                        currentProgress = 1f
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        testState = TestState.Error(e.message ?: "未知錯誤")
                    )
                }
            }
        }
    }

    /**
     * 開始多線程測試
     */
    fun startMultiThreadTest() {
        val coreCount = _uiState.value.cpuCores

        // 取消之前的測試
        currentJob?.cancel()

        _uiState.update { state ->
            state.copy(
                testState = TestState.Running(0f, 0),
                currentProgress = 0f,
                currentCore = 0
            )
        }

        currentJob = viewModelScope.launch {
            try {
                val result = BenchmarkEngine.runMultiThreadTest(coreCount) { progress, activeCores ->
                    _uiState.update { state ->
                        state.copy(
                            currentProgress = progress,
                            currentCore = activeCores,
                            testState = TestState.Running(progress, activeCores)
                        )
                    }
                }

                // 更新結果
                _uiState.update { state ->
                    state.copy(
                        multiThreadResult = result,
                        testState = TestState.Completed(result),
                        currentProgress = 1f
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        testState = TestState.Error(e.message ?: "未知錯誤")
                    )
                }
            }
        }
    }

    /**
     * 停止當前測試
     */
    fun stopTest() {
        currentJob?.cancel()
        currentJob = null

        _uiState.update { state ->
            state.copy(
                testState = TestState.Idle,
                currentProgress = 0f,
                currentCore = 0
            )
        }
    }

    /**
     * 重置所有結果
     */
    fun resetResults() {
        stopTest()
        _uiState.update { state ->
            state.copy(
                singleThreadResult = null,
                multiThreadResult = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}

/**
 * UI狀態數據類
 */
data class UiState(
    val deviceName: String = "",
    val cpuCores: Int = 0,
    val cpuType: String = "",
    val testState: TestState = TestState.Idle,
    val currentProgress: Float = 0f,
    val currentCore: Int = 0,
    val singleThreadResult: BenchmarkResult? = null,
    val multiThreadResult: BenchmarkResult? = null
) {
    val isRunning: Boolean
        get() = testState is TestState.Running

    val isIdle: Boolean
        get() = testState is TestState.Idle

    val hasResults: Boolean
        get() = singleThreadResult != null || multiThreadResult != null

    val showComparison: Boolean
        get() = singleThreadResult != null && multiThreadResult != null
}
