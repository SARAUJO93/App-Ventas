package com.novapos.app.model

data class BusinessSettings(
    val businessName: String = "NOVA POS CAFE",
    val address: String = "Av. Principal 123, Ciudad",
    val taxId: String = "NOVA123456789",
    val phone: String = "555-1234-567",
    val footerNote: String = "¡GRACIAS POR SU COMPRA!",
    val autoPrintReceipt: Boolean = false,
    val paperSize: String = "58mm",
    val printerName: String = "Impresora Térmica Bluetooth",
    val isFirstLaunch: Boolean = true,
    val businessSector: String = "General"
)
