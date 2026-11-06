package com.novapos.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.novapos.app.model.BusinessSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BusinessSettingsRepository {
    val settings: StateFlow<BusinessSettings>
    fun loadUserSettings(userEmail: String?)
    fun saveSettings(newSettings: BusinessSettings)
}

@Singleton
class BusinessSettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : BusinessSettingsRepository {

    private var currentEmail: String? = null

    private fun getPrefsForUser(userEmail: String?): SharedPreferences {
        val cleanName = if (!userEmail.isNullOrBlank()) {
            "nova_pos_settings_" + userEmail.replace(".", "_").replace("@", "_at_")
        } else {
            "nova_pos_settings_default"
        }
        return context.getSharedPreferences(cleanName, Context.MODE_PRIVATE)
    }

    private val _settings = MutableStateFlow(loadSettings(null))
    override val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    override fun loadUserSettings(userEmail: String?) {
        currentEmail = userEmail
        _settings.value = loadSettings(userEmail)
    }

    private fun loadSettings(userEmail: String?): BusinessSettings {
        val prefs = getPrefsForUser(userEmail ?: currentEmail)
        return BusinessSettings(
            businessName = prefs.getString("businessName", "") ?: "",
            address = prefs.getString("address", "Av. Principal 123, Ciudad") ?: "Av. Principal 123, Ciudad",
            taxId = prefs.getString("taxId", "NOVA123456789") ?: "NOVA123456789",
            phone = prefs.getString("phone", "555-1234-567") ?: "555-1234-567",
            footerNote = prefs.getString("footerNote", "¡GRACIAS POR SU COMPRA!") ?: "¡GRACIAS POR SU COMPRA!",
            autoPrintReceipt = prefs.getBoolean("autoPrintReceipt", false),
            paperSize = prefs.getString("paperSize", "58mm") ?: "58mm",
            printerName = prefs.getString("printerName", "Impresora Térmica Bluetooth") ?: "Impresora Térmica Bluetooth",
            isFirstLaunch = prefs.getBoolean("isFirstLaunch", true),
            businessSector = prefs.getString("businessSector", "General") ?: "General"
        )
    }

    override fun saveSettings(newSettings: BusinessSettings) {
        val prefs = getPrefsForUser(currentEmail)
        prefs.edit()
            .putString("businessName", newSettings.businessName)
            .putString("address", newSettings.address)
            .putString("taxId", newSettings.taxId)
            .putString("phone", newSettings.phone)
            .putString("footerNote", newSettings.footerNote)
            .putBoolean("autoPrintReceipt", newSettings.autoPrintReceipt)
            .putString("paperSize", newSettings.paperSize)
            .putString("printerName", newSettings.printerName)
            .putBoolean("isFirstLaunch", newSettings.isFirstLaunch)
            .putString("businessSector", newSettings.businessSector)
            .apply()
        _settings.value = newSettings
    }
}