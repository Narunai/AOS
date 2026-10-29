package org.agentos.poc.collector

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.agentos.poc.collector.model.CompactDom

class ContextAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var debouncer: EventDebouncer

    companion object {
        private const val TAG = "AgentAccessibility"
        private var instance: ContextAccessibilityService? = null
        private var snapshotListener: ((CompactDom) -> Unit)? = null

        fun getInstance(): ContextAccessibilityService? = instance

        fun setOnSnapshotListener(listener: ((CompactDom) -> Unit)?) {
            snapshotListener = listener
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        debouncer = EventDebouncer(serviceScope, debounceDelayMs = 1200L)
        Log.i(TAG, "ContextAccessibilityService connected and ready")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                debouncer.executeImmediate {
                    captureCurrentScreen(packageName)
                }
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                debouncer.scheduleDebounced {
                    captureCurrentScreen(packageName)
                }
            }
        }
    }

    private fun captureCurrentScreen(packageName: String) {
        val root = rootInActiveWindow ?: return
        try {
            val dom = DomPruner.prune(
                root = root,
                packageName = packageName
            ) ?: return

            if (debouncer.shouldRecordHash(dom.contentHash)) {
                Log.d(TAG, "Captured new Compact DOM from $packageName (${dom.nodes.size} nodes, hash=${dom.contentHash})")
                snapshotListener?.invoke(dom)
            }
        } finally {
            root.recycle()
        }
    }

    /**
     * Finds and clicks a node by its sequential ID in the current active window.
     */
    fun clickNodeById(targetId: Int): Boolean {
        val root = rootInActiveWindow ?: return false
        try {
            var counter = 1
            return findAndClick(root, targetId) { counter++ }
        } finally {
            root.recycle()
        }
    }

    private fun findAndClick(node: AccessibilityNodeInfo, targetId: Int, nextId: () -> Int): Boolean {
        val currentId = nextId()
        if (currentId == targetId) {
            return if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                // If not clickable itself, try parent
                var parent = node.parent
                var clicked = false
                while (parent != null) {
                    if (parent.isClickable) {
                        clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        break
                    }
                    val nextParent = parent.parent
                    parent.recycle()
                    parent = nextParent
                }
                clicked
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findAndClick(child, targetId, nextId)
            child.recycle()
            if (found) return true
        }
        return false
    }

    /**
     * Injects text into an editable node by sequential ID.
     */
    fun inputTextById(targetId: Int, text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        try {
            var counter = 1
            return findAndInput(root, targetId, text) { counter++ }
        } finally {
            root.recycle()
        }
    }

    private fun findAndInput(node: AccessibilityNodeInfo, targetId: Int, text: String, nextId: () -> Int): Boolean {
        val currentId = nextId()
        if (currentId == targetId) {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findAndInput(child, targetId, text, nextId)
            child.recycle()
            if (found) return true
        }
        return false
    }

    /**
     * Injects gesture tap at specific screen coordinates.
     */
    fun clickCoordinates(x: Float, y: Float, callback: ((Boolean) -> Unit)? = null) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 80)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                callback?.invoke(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                callback?.invoke(false)
            }
        }, null)
    }

    override fun onInterrupt() {
        Log.w(TAG, "ContextAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        debouncer.clear()
        if (instance == this) {
            instance = null
        }
    }
}

