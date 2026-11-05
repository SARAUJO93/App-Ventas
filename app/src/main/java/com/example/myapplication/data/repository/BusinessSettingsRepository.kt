package com.example.myapplication.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.BusinessSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface BusinessSettingsRepository {
    val settings: StateFlow<BusinessSettings>
    fun saveSettings(newSettings: BusinessSettings)
}

@Singleton
class BusinessSettingsRepositoryImpl @Inject constructor(
    context: Context
) : BusinessSettingsRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nova_pos_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    override val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

    private fun loadSettings(): BusinessSettings {
        return BusinessSettings(
            businessName = prefs.getString("businessName", "NOVA POS CAFE") ?: "NOVA POS CAFE",
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