package org.agentos.poc.collector

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Bridge communicating with Shizuku to execute privileged shell commands (UID 2000)
 * without needing root or modifying the OS bootloader.
 */
object ShizukuBridge {

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun hasPermission(): Boolean {
        return if (!isShizukuAvailable()) {
            false
        } else if (Shizuku.isPreV11()) {
            false
        } else {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission(requestCode: Int) {
        if (isShizukuAvailable() && !hasPermission()) {
            Shizuku.requestPermission(requestCode)
        }
    }

    /**
     * Executes a shell command via Shizuku.
     */
    fun exec(command: String): String {
        if (!hasPermission()) {
            return "ERROR: Shizuku permission not granted"
        }

        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    /**
     * Retrieves the exact focused activity from ActivityTaskManager.
     */
    fun getTopActivity(): String? {
        val raw = exec("dumpsys activity top | grep 'ACTIVITY'")
        // Example output: "ACTIVITY jp.naver.line.android/.activity.chathistory.ChatHistoryActivity 8f9b90"
        val regex = Regex("ACTIVITY\\s+([^\\s]+)/([^\\s]+)")
        val match = regex.find(raw)
        return match?.let { "${it.groupValues[1]}/${it.groupValues[2]}" }
    }
}

