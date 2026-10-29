package org.agentos.poc.privacy

/**
 * Guardrail preventing autonomous execution on sensitive buttons (Money transfer, account deletion, etc.)
 * without explicit user confirmation.
 */
object ActionGuardrail {

    private val HIGH_RISK_KEYWORDS = listOf(
        "โอนเงิน", "ชำระเงิน", "จ่ายเงิน", "ยืนยันการโอน", "ยืนยันคำสั่งซื้อ",
        "ลบบัญชี", "ลบถาวร", "รีเซ็ต", "ยกเลิกการสมัคร", "ลบข้อมูล",
        "transfer", "pay", "checkout", "confirm payment", "delete account",
        "delete permanently", "factory reset", "send money"
    )

    /**
     * Inspects target node description, text, or hint to check if human confirmation is required.
     */
    fun requiresUserConfirmation(actionDescription: String?, nodeText: String?): Boolean {
        val combined = "${actionDescription.orEmpty()} ${nodeText.orEmpty()}".lowercase()
        return HIGH_RISK_KEYWORDS.any { keyword ->
            combined.contains(keyword.lowercase())
        }
    }
}

