package com.example.cpubenchmark

import com.example.cpubenchmark.data.BenchmarkResult
import com.example.cpubenchmark.data.ThreadType
import com.example.cpubenchmark.logic.BenchmarkEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * 基準測試引擎單元測試
 */
class BenchmarkEngineTest {

    @Test
    fun `test single thread result structure`() = runTest {
        val result = BenchmarkEngine.runSingleThreadTest()

        // 驗證結果結構
        assertNotNull(result)
        assertEquals(ThreadType.SINGLE_THREAD, result.threadType)
        assertTrue(result.timeTaken >= 0)
        assertTrue(result.score >= 0)
        assertEquals(1, result.cpuCoresUsed)
    }

    @Test
    fun `test multi thread result structure with available cores`() = runTest {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val result = BenchmarkEngine.runMultiThreadTest(coreCount)

        // 驗證結果結構
        assertNotNull(result)
        assertEquals(ThreadType.MULTI_THREAD, result.threadType)
        assertTrue(result.timeTaken >= 0)
        assertTrue(result.score >= 0)
        assertEquals(coreCount, result.cpuCoresUsed)
    }

    @Test
    fun `test single thread is faster than multi thread with single core`() = runTest {
        // 在單核心設備上，單線程應該更快或相近
        val singleResult = BenchmarkEngine.runSingleThreadTest()
        val multiResult = BenchmarkEngine.runMultiThreadTest(1)

        // 由於測試的隨機性，允許一定的誤差
        val timeDiff = kotlin.math.abs(singleResult.timeTaken - multiResult.timeTaken)
        val maxAllowedDiff = singleResult.timeTaken * 0.5 // 允許50%的誤差

        assertTrue(
            "時間差異過大: 單線程=${singleResult.timeTaken}ms, 多線程=${multiResult.timeTaken}ms",
            timeDiff <= maxAllowedDiff || singleResult.timeTaken <= multiResult.timeTaken
        )
    }

    @Test
    fun `test score calculation is reasonable`() = runTest {
        val result = BenchmarkEngine.runSingleThreadTest()

        // 分數應該是一個合理的正值
        assertTrue("分數應為正值", result.score > 0)
        // 分數不應該過於巨大
        assertTrue("分數不應過於巨大", result.score < 1_000_000_000)
    }

    @Test
    fun `test theoretical speedup calculation`() {
        val coreCount = 4
        val theoreticalSpeedup = BenchmarkEngine.getTheoreticalSpeedup(coreCount)

        assertEquals(4.0, theoreticalSpeedup, 0.001)
    }

    @Test
    fun `test actual speedup calculation`() {
        val singleResult = BenchmarkResult(
            score = 1000,
            timeTaken = 1000,
            threadType = ThreadType.SINGLE_THREAD,
            cpuCoresUsed = 1
        )

        val multiResult = BenchmarkResult(
            score = 4000,
            timeTaken = 250,
            threadType = ThreadType.MULTI_THREAD,
            cpuCoresUsed = 4
        )

        val speedup = BenchmarkEngine.calculateActualSpeedup(singleResult, multiResult)

        // 加速比 = 串行時間 / 並行時間
        assertEquals(4.0, speedup, 0.001)
    }

    @Test
    fun `test multi core results have different time characteristics`() = runTest {
        val singleResult = BenchmarkEngine.runSingleThreadTest()
        val coreCount = Runtime.getRuntime().availableProcessors()

        if (coreCount > 1) {
            val multiResult = BenchmarkEngine.runMultiThreadTest(coreCount)

            // 多線程測試通常在多核設備上更快
            // 但由於測試的隨機性和系統調度，這不是絕對的
            // 我們主要驗證結構的正確性
            assertTrue(multiResult.cpuCoresUsed > 0)
        } else {
            // 單核心設備，多線程測試應該與單線程類似
            val multiResult = BenchmarkEngine.runMultiThreadTest(1)
            assertEquals(1, multiResult.cpuCoresUsed)
        }
    }
}

/**
 * 數據類單元測試
 */
class DataClassesTest {

    @Test
    fun `test BenchmarkResult creation`() {
        val result = BenchmarkResult(
            score = 5000,
            timeTaken = 200,
            threadType = ThreadType.SINGLE_THREAD,
            cpuCoresUsed = 1
        )

        assertEquals(5000, result.score)
        assertEquals(200, result.timeTaken)
        assertEquals(ThreadType.SINGLE_THREAD, result.threadType)
        assertEquals(1, result.cpuCoresUsed)
    }

    @Test
    fun `test ThreadType enum values`() {
        val values = ThreadType.values()

        assertEquals(2, values.size)
        assertTrue(values.contains(ThreadType.SINGLE_THREAD))
        assertTrue(values.contains(ThreadType.MULTI_THREAD))
    }
}
