package org.agentos.poc.privacy

import java.util.regex.Pattern

/**
 * High-performance deterministic Regex and Checksum masker for Personally Identifiable Information (PII).
 * Executes in RAM before snapshots are saved to database or sent to language models.
 */
object PiiRegexMasker {

    // Credit Card (13-19 digits, optionally spaced or hyphenated)
    private val CREDIT_CARD_REGEX = Pattern.compile(
        "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12}|(?:2131|1800|35\\d{3})\\d{11}|[0-9]{4}[- ]?[0-9]{4}[- ]?[0-9]{4}[- ]?[0-9]{4})\\b"
    )

    // Thai National ID (13 digits: X-XXXX-XXXXX-XX-X or 13 consecutive digits)
    private val THAI_ID_REGEX = Pattern.compile(
        "\\b[1-9]\\d{12}\\b|\\b[1-9]-\\d{4}-\\d{5}-\\d{2}-\\d\\b"
    )

    // OTP patterns (e.g., "OTP: 123456", "รหัส OTP 849201", "verification code is 492014")
    private val OTP_REGEX = Pattern.compile(
        "(?i)(?:otp|code|รหัสยืนยัน|รหัส otp|verification code)[^0-9]{1,10}([0-9]{4,8})\\b"
    )

    // Phone number pattern (Thai format: 08x-xxx-xxxx, 09x-xxx-xxxx, 06x-xxx-xxxx)
    private val THAI_PHONE_REGEX = Pattern.compile(
        "\\b(0[689]\\d[- ]?\\d{3}[- ]?\\d{4})\\b"
    )

    /**
     * Sanitizes input text by redacting sensitive data.
     */
    fun maskAll(input: String): String {
        if (input.isBlank()) return input

        var result = input

        // 1. Redact OTPs
        val otpMatcher = OTP_REGEX.matcher(result)
        val otpBuffer = StringBuffer()
        while (otpMatcher.find()) {
            val fullMatch = otpMatcher.group(0) ?: continue
            val digits = otpMatcher.group(1) ?: continue
            val masked = fullMatch.replace(digits, "[OTP_HIDDEN]")
            otpMatcher.appendReplacement(otpBuffer, java.util.regex.Matcher.quoteReplacement(masked))
        }
        otpMatcher.appendTail(otpBuffer)
        result = otpBuffer.toString()

        // 2. Redact Credit / Debit Cards
        result = CREDIT_CARD_REGEX.matcher(result).replaceAll("[CARD_REDACTED]")

        // 3. Redact Thai National ID
        val thaiIdMatcher = THAI_ID_REGEX.matcher(result)
        val thaiIdBuffer = StringBuffer()
        while (thaiIdMatcher.find()) {
            val raw = thaiIdMatcher.group(0) ?: continue
            val candidate = raw.replace("-", "")
            if (isValidThaiNationalId(candidate)) {
                thaiIdMatcher.appendReplacement(thaiIdBuffer, "[THAI_ID_REDACTED]")
            } else {
                thaiIdMatcher.appendReplacement(thaiIdBuffer, java.util.regex.Matcher.quoteReplacement(raw))
            }
        }
        thaiIdMatcher.appendTail(thaiIdBuffer)
        result = thaiIdBuffer.toString()

        return result
    }

    /**
     * Checksum algorithm for 13-digit Thai National ID.
     */
    fun isValidThaiNationalId(id: String): Boolean {
        if (id.length != 13 || !id.all { it.isDigit() }) return false
        var sum = 0
        for (i in 0 until 12) {
            sum += id[i].digitToInt() * (13 - i)
        }
        val remainder = sum % 11
        val checkDigit = (11 - remainder) % 10
        return checkDigit == id[12].digitToInt()
    }
}

