package org.agentos.poc.memory

import org.agentos.poc.memory.search.SimHashDeduplicator
import org.junit.Assert.*
import org.junit.Test

class SimHashDeduplicatorTest {

    @Test
    fun testIdenticalContentYieldsSameHash() {
        val text = "คอนโด Noble อนุสาวรีย์ ชั้น 18 ปล่อยเช่า 16,500 บาท"
        val hash1 = SimHashDeduplicator.computeSimHash(text)
        val hash2 = SimHashDeduplicator.computeSimHash(text)

        assertEquals(hash1, hash2)
        assertEquals(0, SimHashDeduplicator.hammingDistance(hash1, hash2))
        assertTrue(SimHashDeduplicator.isNearDuplicate(hash1, hash2))
    }

    @Test
    fun testNearDuplicateContentDetected() {
        val original = "คอนโด Noble อนุสาวรีย์ ชั้น 18 ปล่อยเช่า 16,500 บาท นัดดูห้องวันเสาร์ บ่ายสองโมง"
        val slightlyModified = "คอนโด Noble อนุสาวรีย์ ชั้น 18 ปล่อยเช่า 16,500 บาท นัดดูห้องวันเสาร์ บ่ายสองโมง ขอบคุณครับ"

        val hash1 = SimHashDeduplicator.computeSimHash(original)
        val hash2 = SimHashDeduplicator.computeSimHash(slightlyModified)

        val distance = SimHashDeduplicator.hammingDistance(hash1, hash2)
        assertTrue("Hamming distance $distance should be <= 9 for near duplicates", distance <= 9)
        assertTrue(SimHashDeduplicator.isNearDuplicate(hash1, hash2))
    }

    @Test
    fun testCompletelyDifferentContentNotDuplicate() {
        val text1 = "คอนโด Noble อนุสาวรีย์ ปล่อยเช่า 16,500 บาท นัดดูห้องวันเสาร์"
        val text2 = "สั่งข้าวมันไก่พิเศษหนัง จากร้านนายอ้วน ประตูน้ำ จัดส่งด้วย GrabFood ด่วน"

        val hash1 = SimHashDeduplicator.computeSimHash(text1)
        val hash2 = SimHashDeduplicator.computeSimHash(text2)

        val distance = SimHashDeduplicator.hammingDistance(hash1, hash2)
        assertTrue("Distance $distance should be greater than 9 for different content", distance > 9)
        assertFalse(SimHashDeduplicator.isNearDuplicate(hash1, hash2))
    }
}

