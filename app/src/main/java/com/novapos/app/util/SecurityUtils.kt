package com.novapos.app.util

import java.security.MessageDigest

object SecurityUtils {

    fun hashPin(pin: String, userEmail: String? = null): String {
        if (pin.isEmpty()) return ""
        val salt = if (!userEmail.isNullOrBlank()) userEmail.trim().lowercase() else "NOVA_POS_SALT_V1_2026"
        val saltedPin = "$salt:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedPin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(plainPin: String, hashedPin: String, userEmail: String? = null): Boolean {
        if (plainPin.isEmpty() || hashedPin.isEmpty()) return false
        return if (hashedPin.length == 64) {
            hashPin(plainPin, userEmail) == hashedPin ||
                    hashPin(plainPin, null) == hashedPin ||
                    hashUnsaltedLegacy(plainPin) == hashedPin
        } else {
            plainPin == hashedPin
        }
    }

    private fun hashUnsaltedLegacy(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}