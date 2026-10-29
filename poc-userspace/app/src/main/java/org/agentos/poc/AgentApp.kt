package org.agentos.poc

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.agentos.poc.collector.ContextAccessibilityService
import org.agentos.poc.collector.ContextNotificationListener
import org.agentos.poc.memory.repository.MemoryRepository

class AgentApp : Application() {

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    lateinit var memoryRepository: MemoryRepository
        private set

    companion object {
        private const val TAG = "AgentApp"
        lateinit var instance: AgentApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        memoryRepository = MemoryRepository(this)

        setupContextPipelines()
        Log.i(TAG, "AgentOS application initialized")
    }

    private fun setupContextPipelines() {
        // Wire Accessibility DOM snapshots directly into Episodic Memory
        ContextAccessibilityService.setOnSnapshotListener { dom ->
            appScope.launch {
                val appLabel = try {
                    val appInfo = packageManager.getApplicationInfo(dom.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    dom.packageName
                }

                val saved = memoryRepository.recordSnapshot(dom, appLabel)
                if (saved) {
                    Log.d(TAG, "Recorded episodic memory for $appLabel (${dom.nodes.size} nodes)")
                }
            }
        }

        // Wire Notification stream into Context
        ContextNotificationListener.setOnNotificationListener { notif ->
            Log.d(TAG, "Notification received from ${notif.appName}: ${notif.title}")
        }
    }
}

