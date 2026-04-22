package com.silentsos.app.utils

object PhoneNumberFormatter {

    private val nonDialableCharacters = Regex("[^+\\d]")
    private val e164Pattern = Regex("^\\+[1-9]\\d{6,14}$")

    fun sanitize(rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return ""

        val normalized = trimmed.replace(nonDialableCharacters, "")
        return when {
            normalized.startsWith("00") -> "+${normalized.drop(2)}"
            normalized.startsWith("+") -> normalized
            else -> normalized
        }
    }

    fun isValidE164(rawValue: String): Boolean {
        return e164Pattern.matches(sanitize(rawValue))
    }
}
