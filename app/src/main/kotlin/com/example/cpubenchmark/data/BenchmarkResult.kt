package com.example.cpubenchmark.data

/**
 * 基準測試結果數據類
 * @param score 計算得分（分數越高性能越好）
 * @param timeTaken 耗時（毫秒）
 * @param threadType 執行緒類型（單線程/多線程）
 * @param cpuCoresUsed 使用的CPU核心數
 */
data class BenchmarkResult(
    val score: Long,
    val timeTaken: Long,
    val threadType: ThreadType,
    val cpuCoresUsed: Int
)

/**
 * 執行緒類型枚舉
 */
enum class ThreadType {
    SINGLE_THREAD,
    MULTI_THREAD
}

/**
 * 測試狀態密封類
 */
sealed class TestState {
    data object Idle : TestState()
    data class Running(val progress: Float, val currentCore: Int = 0) : TestState()
    data class Completed(val result: BenchmarkResult) : TestState()
    data class Error(val message: String) : TestState()
}
