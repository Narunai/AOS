package org.agentos.poc.privacy

import org.junit.Assert.*
import org.junit.Test

class PiiRegexMaskerTest {

    @Test
    fun testCreditCardMasking() {
        val input = "Please charge my Visa card 4111 2222 3333 4444 for the booking."
        val masked = PiiRegexMasker.maskAll(input)
        assertFalse(masked.contains("4111"))
        assertTrue(masked.contains("[CARD_REDACTED]"))
    }

    @Test
    fun testOtpCodeMasking() {
        val inputEn = "Your verification OTP code is 492014, valid for 5 minutes."
        val maskedEn = PiiRegexMasker.maskAll(inputEn)
        assertFalse(maskedEn.contains("492014"))
        assertTrue(maskedEn.contains("[OTP_HIDDEN]"))

        val inputTh = "รหัส OTP ของคุณคือ 849201 อย่าเปิดเผยให้ผู้อื่น"
        val maskedTh = PiiRegexMasker.maskAll(inputTh)
        assertFalse(maskedTh.contains("849201"))
        assertTrue(maskedTh.contains("[OTP_HIDDEN]"))
    }

    @Test
    fun testThaiNationalIdChecksum() {
        // Valid Thai ID example (passes modulo 11 checksum): 1-1004-99905-24-1
        // 1*13 + 1*12 + 0*11 + 0*10 + 4*9 + 9*8 + 9*7 + 9*6 + 0*5 + 5*4 + 2*3 + 4*2 = 13 + 12 + 0 + 0 + 36 + 72 + 63 + 54 + 0 + 20 + 6 + 8 = 284
        // 284 % 11 = 9. 11 - 9 = 2 % 10 = 2 -> so check digit 2
        // Let's test checksum helper
        assertTrue(PiiRegexMasker.isValidThaiNationalId("1100499905242"))
        assertFalse(PiiRegexMasker.isValidThaiNationalId("1234567890123")) // Invalid checksum
    }

    @Test
    fun testBankingPackageBlacklist() {
        assertTrue(BankingPackageRegistry.isPackageBlacklisted("com.kasikorn.retail.mbanking.wap"))
        assertTrue(BankingPackageRegistry.isPackageBlacklisted("com.scb.phone"))
        assertTrue(BankingPackageRegistry.isPackageBlacklisted("com.x8bit.bitwarden"))
        assertFalse(BankingPackageRegistry.isPackageBlacklisted("jp.naver.line.android"))
        assertFalse(BankingPackageRegistry.isPackageBlacklisted("com.google.android.keep"))
    }

    @Test
    fun testActionGuardrails() {
        assertTrue(ActionGuardrail.requiresUserConfirmation("Click", "ยืนยันการโอนเงิน"))
        assertTrue(ActionGuardrail.requiresUserConfirmation("Click", "Delete Account Permanently"))
        assertFalse(ActionGuardrail.requiresUserConfirmation("Click", "Open Menu"))
        assertFalse(ActionGuardrail.requiresUserConfirmation("Click", "ตกลง"))
    }
}

