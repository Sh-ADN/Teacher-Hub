package com.abutorab.teacher.hub.util

object NumeralFormat {
    fun localize(value: String, useBengali: Boolean = true): String {
        if (!useBengali) return value
        return value.map { char ->
            if (char.isDigit()) {
                val bengaliDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
                bengaliDigits[char - '0']
            } else {
                char
            }
        }.joinToString("")
    }
}
