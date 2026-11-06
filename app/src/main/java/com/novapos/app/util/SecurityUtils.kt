package com.novapos.app.util

import java.security.MessageDigest

object SecurityUtils {

    fun hashPin(pin: String): String {
        if (pin.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(plainPin: String, hashedPin: String): Boolean {
        if (plainPin.isEmpty() || hashedPin.isEmpty()) return false
        // Si el hash almacenado tiene longitud de SHA-256 hex (64 caracteres)
        return if (hashedPin.length == 64) {
            hashPin(plainPin) == hashedPin
        } else {
            // Compatibilidad legacy si el PIN no había sido hasheado previamente
            plainPin == hashedPin
        }
    }
}