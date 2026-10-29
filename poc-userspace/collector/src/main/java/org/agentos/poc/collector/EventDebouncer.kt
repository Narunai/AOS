package org.agentos.poc.collector

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debounces rapid screen change events (e.g. during fast flinging or scrolling).
 * Prevents battery drain and unnecessary snapshot allocations.
 */
class EventDebouncer(
    private val scope: CoroutineScope,
    private val debounceDelayMs: Long = 1200L
) {
    private var debounceJob: Job? = null
    private var lastCapturedHash: String? = null

    /**
     * Schedules a debounced snapshot task when the screen stops changing.
     */
    fun scheduleDebounced(action: suspend () -> Unit) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceDelayMs)
            action()
        }
    }

    /**
     * Executes immediately for high-priority single interactions (e.g. User clicked a button).
     */
    fun executeImmediate(action: suspend () -> Unit) {
        debounceJob?.cancel()
        scope.launch {
            action()
        }
    }

    /**
     * Deduplicates based on hash. Returns true if hash is new.
     */
    fun shouldRecordHash(newHash: String): Boolean {
        if (newHash == lastCapturedHash) {
            return false
        }
        lastCapturedHash = newHash
        return true
    }

    fun clear() {
        debounceJob?.cancel()
        lastCapturedHash = null
    }
}

