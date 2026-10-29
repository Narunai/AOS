package org.agentos.poc.privacy

sealed class FilterResult {
    object Allowed : FilterResult()
    data class Sanitized(val sanitizedText: String) : FilterResult()
    object Blocked : FilterResult()
}

/**
 * High-level Sensitive Filter for screen nodes and windows.
 */
object SensitiveFilter {

    /**
     * Checks if an entire window snapshot should be blocked based on its package name.
     */
    fun shouldBlockPackage(packageName: String?): Boolean {
        return BankingPackageRegistry.isPackageBlacklisted(packageName)
    }

    /**
     * Inspects a single node's text and password state.
     */
    fun filterNodeText(
        rawText: String?,
        isPassword: Boolean
    ): String {
        if (isPassword) {
            return "[PASSWORD_PROTECTED]"
        }
        if (rawText.isNullOrBlank()) {
            return ""
        }
        return PiiRegexMasker.maskAll(rawText)
    }
}

