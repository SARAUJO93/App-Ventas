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

    private fun getFirebaseUserEmail(): String? {
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
        } catch (e: Exception) {
            null
        }
    }

    private fun loadSettings(userEmail: String?): BusinessSettings {
        val activeEmail = userEmail ?: currentEmail ?: getFirebaseUserEmail()
        val userPrefs = getPrefsForUser(activeEmail)
        val defaultPrefs = getPrefsForUser(null)

        var name = userPrefs.getString("businessName", "") ?: ""
        if (name.isBlank()) {
            name = defaultPrefs.getString("businessName", "") ?: ""
        }

        var isFirst = userPrefs.getBoolean("isFirstLaunch", true)
        if (name.isBlank()) {
            isFirst = true
        }

        return BusinessSettings(
            businessName = name,
            address = userPrefs.getString("address", defaultPrefs.getString("address", "Av. Principal 123, Ciudad")) ?: "Av. Principal 123, Ciudad",
            taxId = userPrefs.getString("taxId", defaultPrefs.getString("taxId", "NOVA123456789")) ?: "NOVA123456789",
            phone = userPrefs.getString("phone", defaultPrefs.getString("phone", "555-1234-567")) ?: "555-1234-567",
            footerNote = userPrefs.getString("footerNote", defaultPrefs.getString("footerNote", "¡GRACIAS POR SU COMPRA!")) ?: "¡GRACIAS POR SU COMPRA!",
            autoPrintReceipt = userPrefs.getBoolean("autoPrintReceipt", defaultPrefs.getBoolean("autoPrintReceipt", false)),
            paperSize = userPrefs.getString("paperSize", defaultPrefs.getString("paperSize", "58mm")) ?: "58mm",
            printerName = userPrefs.getString("printerName", defaultPrefs.getString("printerName", "Impresora Térmica Bluetooth")) ?: "Impresora Térmica Bluetooth",
            isFirstLaunch = isFirst,
            businessSector = userPrefs.getString("businessSector", defaultPrefs.getString("businessSector", "General")) ?: "General"
        )
    }

    override fun saveSettings(newSettings: BusinessSettings) {
        val activeEmail = currentEmail ?: getFirebaseUserEmail()
        val userPrefs = getPrefsForUser(activeEmail)
        val defaultPrefs = getPrefsForUser(null)

        val writeToPrefs = { prefs: SharedPreferences ->
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
        }

        writeToPrefs(userPrefs)
        writeToPrefs(defaultPrefs)

        _settings.value = newSettings
    }
}