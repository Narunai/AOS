package org.agentos.poc.memory.search

import java.security.MessageDigest

/**
 * 64-bit SimHash algorithm for detecting near-duplicate screen contents (> 85% similarity).
 * Computes in < 1ms on Android CPU without neural networks.
 */
object SimHashDeduplicator {

    fun computeSimHash(text: String): Long {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return 0L

        val v = IntArray(64)
        for (token in tokens) {
            val hash = hash64(token)
            for (i in 0 until 64) {
                val bit = (hash shr i) and 1L
                if (bit == 1L) {
                    v[i] += 1
                } else {
                    v[i] -= 1
                }
            }
        }

        var simhash = 0L
        for (i in 0 until 64) {
            if (v[i] > 0) {
                simhash = simhash or (1L shl i)
            }
        }
        return simhash
    }

    /**
     * Calculates Hamming distance between two 64-bit SimHashes (number of different bits).
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    /**
     * Returns true if two screens are near-duplicates (similarity >= 85%, distance <= 9 bits).
     */
    fun isNearDuplicate(hash1: Long, hash2: Long, maxDistance: Int = 9): Boolean {
        return hammingDistance(hash1, hash2) <= maxDistance
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .split(Regex("[\\s,.:;!?'\"()\\[\\]{}|/\\\\]+"))
            .filter { it.length >= 2 }
    }

    private fun hash64(token: String): Long {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(token.toByteArray(Charsets.UTF_8))
        var h = 0L
        for (i in 0 until 8) {
            h = (h shl 8) or (bytes[i].toLong() and 0xFF)
        }
        return h
    }
}

