package com.example.cpubenchmark.logic

import com.example.cpubenchmark.data.BenchmarkResult
import com.example.cpubenchmark.data.ThreadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.pow
import kotlin.random.Random

/**
 * 基準測試引擎
 * 包含各種CPU密集型計算任務
 */
object BenchmarkEngine {

    // 測試參數配置
    private const val FIBONACCI_ITERATIONS = 10000
    private const val MATRIX_SIZE = 100
    private const val MONTE_CARLO_ITERATIONS = 50000
    private const val PRIME_CALCULATION_LIMIT = 100000

    /**
     * 計算基準測試得分
     * @param timeTaken 耗時（毫秒）
     * @return 標準化得分
     */
    private fun calculateScore(timeTaken: Long): Long {
        // 基準時間為1000毫秒，基準分數為10000分
        // 分數 = (基準時間 / 實際時間) × 基準分數
        return if (timeTaken > 0) {
            (1000000L / timeTaken) * 100
        } else {
            0
        }
    }

    /**
     * 執行單線程基準測試
     * @param onProgressCallback 進度回調函數
     * @return 測試結果
     */
    suspend fun runSingleThreadTest(
        onProgressCallback: (Float) -> Unit = {}
    ): BenchmarkResult {
        val startTime = System.currentTimeMillis()

        // 執行混合計算任務
        val iterations = 50
        for (i in 0 until iterations) {
            // 斐波那契數列計算
            calculateFibonacci(FIBONACCI_ITERATIONS)

            // 矩陣乘法
            performMatrixMultiplication(MATRIX_SIZE)

            // 蒙特卡洛方法計算圓周率
            calculateMonteCarloPi(MONTE_CARLO_ITERATIONS)

            // 質數計算
            calculatePrimes(PRIME_CALCULATION_LIMIT)

            // 更新進度
            onProgressCallback((i + 1).toFloat() / iterations)
        }

        val timeTaken = System.currentTimeMillis() - startTime
        val score = calculateScore(timeTaken)

        return BenchmarkResult(
            score = score,
            timeTaken = timeTaken,
            threadType = ThreadType.SINGLE_THREAD,
            cpuCoresUsed = 1
        )
    }

    /**
     * 執行多線程基準測試
     * @param coreCount 使用的CPU核心數
     * @param onProgressCallback 進度回調函數
     * @return 測試結果
     */
    suspend fun runMultiThreadTest(
        coreCount: Int,
        onProgressCallback: (Float, Int) -> Unit = { _, _ -> }
    ): BenchmarkResult = coroutineScope {
        val startTime = System.currentTimeMillis()

        // 每個核心執行的任務量
        val iterationsPerCore = 50 / coreCount
        if (iterationsPerCore == 0) {
            // 如果核心數過多，減少每個核心的任務量
            return@coroutineScope runMultiThreadFixedTasks(coreCount, onProgressCallback)
        }

        // 並行執行所有核心的任務
        val tasks = (0 until coreCount).map { coreId ->
            async(Dispatchers.Default) {
                repeat(iterationsPerCore) { iteration ->
                    calculateFibonacci(FIBONACCI_ITERATIONS)
                    performMatrixMultiplication(MATRIX_SIZE)
                    calculateMonteCarloPi(MONTE_CARLO_ITERATIONS)
                    calculatePrimes(PRIME_CALCULATION_LIMIT)

                    // 計算總進度
                    val totalProgress = ((coreId * iterationsPerCore + iteration + 1).toFloat() / coreCount / iterationsPerCore)
                    onProgressCallback(totalProgress, coreId)
                }
            }
        }

        // 等待所有任務完成
        awaitAll(*tasks.toTypedArray())

        val timeTaken = System.currentTimeMillis() - startTime
        val score = calculateScore(timeTaken)

        BenchmarkResult(
            score = score,
            timeTaken = timeTaken,
            threadType = ThreadType.MULTI_THREAD,
            cpuCoresUsed = coreCount
        )
    }

    /**
     * 多線程測試的固定任務版本（當核心數過多時使用）
     */
    private suspend fun runMultiThreadFixedTasks(
        coreCount: Int,
        onProgressCallback: (Float, Int) -> Unit
    ): BenchmarkResult = coroutineScope {
        val startTime = System.currentTimeMillis()
        val totalTasks = 50

        val tasks = (0 until coreCount).map { coreId ->
            async(Dispatchers.Default) {
                // 每個核心執行一個任務
                val tasksPerCore = totalTasks / coreCount
                repeat(tasksPerCore) { iteration ->
                    calculateFibonacci(FIBONACCI_ITERATIONS)
                    performMatrixMultiplication(MATRIX_SIZE)
                    calculateMonteCarloPi(MONTE_CARLO_ITERATIONS)
                    calculatePrimes(PRIME_CALCULATION_LIMIT)

                    val totalProgress = ((coreId * tasksPerCore + iteration + 1).toFloat() / totalTasks)
                    onProgressCallback(totalProgress, coreId)
                }
            }
        }

        awaitAll(*tasks.toTypedArray())

        val timeTaken = System.currentTimeMillis() - startTime
        val score = calculateScore(timeTaken)

        BenchmarkResult(
            score = score,
            timeTaken = timeTaken,
            threadType = ThreadType.MULTI_THREAD,
            cpuCoresUsed = coreCount
        )
    }

    /**
     * 斐波那契數列計算（迭代版本，避免棧溢出）
     * 測試整數運算能力
     */
    private fun calculateFibonacci(n: Int): Long {
        if (n <= 1) return n.toLong()

        var a = 0L
        var b = 1L
        var result = 0L

        for (i in 2..n) {
            result = a + b
            a = b
            b = result
        }

        return result
    }

    /**
     * 矩陣乘法
     * 測試浮點運算和內存訪問性能
     */
    private fun performMatrixMultiplication(size: Int): DoubleArray {
        val matrixA = DoubleArray(size * size) { Random.nextDouble() }
        val matrixB = DoubleArray(size * size) { Random.nextDouble() }
        val result = DoubleArray(size * size)

        for (i in 0 until size) {
            for (j in 0 until size) {
                var sum = 0.0
                for (k in 0 until size) {
                    sum += matrixA[i * size + k] * matrixB[k * size + j]
                }
                result[i * size + j] = sum
            }
        }

        return result
    }

    /**
     * 蒙特卡洛方法計算圓周率
     * 測試隨機數生成和浮點運算能力
     */
    private fun calculateMonteCarloPi(iterations: Int): Double {
        var insideCircle = 0

        for (i in 0 until iterations) {
            val x = Random.nextDouble()
            val y = Random.nextDouble()
            if (x * x + y * y <= 1.0) {
                insideCircle++
            }
        }

        return (insideCircle.toDouble() / iterations) * 4.0
    }

    /**
     * 質數計算
     * 測試整數運算和分支預測
     */
    private fun calculatePrimes(limit: Int): Int {
        var count = 0
        var isPrime: Boolean

        for (n in 2..limit) {
            isPrime = true
            val sqrtN = kotlin.math.sqrt(n.toDouble()).toInt()

            for (i in 2..sqrtN) {
                if (n % i == 0) {
                    isPrime = false
                    break
                }
            }

            if (isPrime) count++
        }

        return count
    }

    /**
     * 獲取理論多核加速比
     * 用於比較實際測試結果
     */
    fun getTheoreticalSpeedup(coreCount: Int): Double {
        // 理想加速比 = 核心數（阿姆達爾定律，無串行部分）
        return coreCount.toDouble()
    }

    /**
     * 計算實際加速比
     */
    fun calculateActualSpeedup(
        singleThreadResult: BenchmarkResult,
        multiThreadResult: BenchmarkResult
    ): Double {
        return if (singleThreadResult.timeTaken > 0) {
            multiThreadResult.timeTaken.toDouble() / singleThreadResult.timeTaken
        } else {
            0.0
        }
    }
}
