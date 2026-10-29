package org.agentos.poc.collector

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import org.agentos.poc.collector.model.CompactDom
import org.agentos.poc.collector.model.NodeItem
import org.agentos.poc.privacy.SensitiveFilter
import java.security.MessageDigest
import java.util.UUID

object DomPruner {

    /**
     * Converts a raw root AccessibilityNodeInfo into a compact, sanitized Semantic DOM.
     * Empty containers and decorative elements are pruned.
     */
    fun prune(
        root: AccessibilityNodeInfo?,
        packageName: String,
        activityName: String? = null,
        windowTitle: String? = null
    ): CompactDom? {
        if (root == null) return null

        // 1. Check if package is strictly blacklisted (e.g. Banking / Passwords)
        if (SensitiveFilter.shouldBlockPackage(packageName)) {
            return null
        }

        val collectedNodes = mutableListOf<NodeItem>()
        var counter = 1

        traverse(root, collectedNodes) {
            counter++
        }

        if (collectedNodes.isEmpty()) {
            return null
        }

        // Calculate content hash for deduplication
        val allText = collectedNodes.joinToString(" ") { "${it.text} ${it.contentDescription.orEmpty()}" }
        val contentHash = computeHash(allText)

        return CompactDom(
            snapshotId = "snap_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            timestampMs = System.currentTimeMillis(),
            packageName = packageName,
            activityName = activityName,
            windowTitle = windowTitle,
            contentHash = contentHash,
            nodes = collectedNodes
        )
    }

    private fun traverse(
        node: AccessibilityNodeInfo,
        outList: MutableList<NodeItem>,
        nextId: () -> Int
    ) {
        val rawText = node.text?.toString()
        val desc = node.contentDescription?.toString()
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val isScrollable = node.isScrollable
        val isPassword = node.isPassword

        // Sanitize text & descriptions
        val sanitizedText = SensitiveFilter.filterNodeText(rawText, isPassword)
        val sanitizedDesc = desc?.let { SensitiveFilter.filterNodeText(it, false) }

        // Pruning logic: keep if it has text, or contentDescription, or is interactive
        val hasMeaningfulContent = sanitizedText.isNotBlank() || !sanitizedDesc.isNullOrBlank()
        val isInteractive = isClickable || isEditable || isScrollable

        if (hasMeaningfulContent || isInteractive) {
            val rect = Rect()
            node.getBoundsInScreen(rect)

            val role = determineRole(node)
            outList.add(
                NodeItem(
                    id = nextId(),
                    role = role,
                    text = sanitizedText,
                    contentDescription = sanitizedDesc,
                    viewIdResourceName = node.viewIdResourceName,
                    clickable = isClickable,
                    editable = isEditable,
                    scrollable = isScrollable,
                    bounds = listOf(rect.left, rect.top, rect.right, rect.bottom)
                )
            )
        }

        // Recursively traverse child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverse(child, outList, nextId)
            child.recycle()
        }
    }

    private fun determineRole(node: AccessibilityNodeInfo): String {
        val className = node.className?.toString()?.lowercase() ?: ""
        return when {
            node.isEditable -> "input"
            className.contains("button") || node.isClickable -> "button"
            className.contains("switch") || className.contains("toggle") -> "switch"
            className.contains("checkbox") -> "checkbox"
            className.contains("edittext") -> "input"
            className.contains("image") -> "image"
            className.contains("scroll") || className.contains("recycler") -> "scroll_container"
            node.isHeading -> "header"
            else -> "text"
        }
    }

    private fun computeHash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}

