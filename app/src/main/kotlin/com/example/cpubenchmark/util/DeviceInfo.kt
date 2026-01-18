package com.example.cpubenchmark.util

import android.os.Build

/**
 * 設備信息工具類
 * 用於獲取設備的CPU信息和系統信息
 */
object DeviceInfo {

    /**
     * 獲取設備型號
     */
    fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }

    /**
     * 獲取CPU核心數
     */
    fun getCpuCoreCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    /**
     * 獲取Android版本
     */
    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    /**
     * 獲取SDK版本號
     */
    fun getSdkVersion(): Int {
        return Build.VERSION.SDK_INT
    }

    /**
     * 獲取完整的設備信息字符串
     */
    fun getFullDeviceInfo(): String {
        return buildString {
            appendLine("設備: ${getDeviceModel()}")
            appendLine("CPU核心數: ${getCpuCoreCount()}")
            appendLine("Android版本: ${getAndroidVersion()} (API ${getSdkVersion()})")
        }
    }

    /**
     * 估算CPU頻率信息（基於設備型號的簡單映射）
     * 注意：這是一個粗略估計，實際頻率需要讀取系統文件
     */
    fun getEstimatedCpuInfo(): String {
        val cores = getCpuCoreCount()
        return when {
            cores <= 4 -> "低功耗型 (${cores}核心)"
            cores in 5..8 -> "效能型 (${cores}核心)"
            cores > 8 -> "旗艦型 (${cores}核心)"
            else -> "未知 (${cores}核心)"
        }
    }
}
