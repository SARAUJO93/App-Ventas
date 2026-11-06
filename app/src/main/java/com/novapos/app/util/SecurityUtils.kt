package com.novapos.app.util

import java.security.MessageDigest

object SecurityUtils {

    private const val SALT = "NOVA_POS_SALT_V1_2026"

    fun hashPin(pin: String): String {
        if (pin.isEmpty()) return ""
        val saltedPin = "$SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedPin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(plainPin: String, hashedPin: String): Boolean {
        if (plainPin.isEmpty() || hashedPin.isEmpty()) return false
        return if (hashedPin.length == 64) {
            hashPin(plainPin) == hashedPin || hashUnsaltedLegacy(plainPin) == hashedPin
        } else {
            plainPin == hashedPin
        }
    }

    private fun hashUnsaltedLegacy(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}