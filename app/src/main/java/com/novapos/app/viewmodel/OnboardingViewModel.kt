package com.novapos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novapos.app.data.repository.BusinessSettingsRepository
import com.novapos.app.data.repository.PosRepository
import com.novapos.app.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: BusinessSettingsRepository,
    private val posRepository: PosRepository
) : ViewModel() {

    private val _selectedSector = MutableStateFlow("")
    val selectedSector: StateFlow<String> = _selectedSector.asStateFlow()

    private val _selectedChallenges = MutableStateFlow<Set<String>>(emptySet())
    val selectedChallenges: StateFlow<Set<String>> = _selectedChallenges.asStateFlow()

    fun selectSector(sector: String) {
        _selectedSector.value = sector
    }

    fun toggleChallenge(challenge: String) {
        val current = _selectedChallenges.value.toMutableSet()
        if (current.contains(challenge)) {
            current.remove(challenge)
        } else {
            current.add(challenge)
        }
        _selectedChallenges.value = current
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            val sector = _selectedSector.value

            // Insertar datos de prueba según el sector
            insertSeedDataForSector(sector)

            // Actualizar configuración
            val currentSettings = settingsRepository.settings.value
            settingsRepository.saveSettings(
                currentSettings.copy(
                    isFirstLaunch = false,
                    businessSector = sector
                )
            )
            onComplete()
        }
    }

    private suspend fun insertSeedDataForSector(sector: String) {
        val seedProducts = when (sector) {
            "Ferretería" -> listOf(
                createProduct("Martillo", 85.0, "Herramientas", 15, 0xFFFFB74D), // Naranja
                createProduct("Tubería PVC", 150.0, "Construcción", 10, 0xFF9E9E9E), // Gris
                createProduct("Clavos (Caja)", 45.0, "Herramientas", 50, 0xFF9E9E9E)
            )
            "Electrónica" -> listOf(
                createProduct("Audífonos", 350.0, "Accesorios", 20, 0xFF4FC3F7), // Azul
                createProduct("Funda de iPhone", 125.0, "Accesorios", 35, 0xFFE0E0E0), // Gris Claro
                createProduct("Cable USB-C", 99.0, "Cables", 40, 0xFF81C784) // Verde
            )
            "Abarrotes" -> listOf(
                createProduct("Coca Cola 600ml", 18.0, "Bebidas", 100, 0xFFE57373), // Rojo
                createProduct("Galletas Emperador", 22.0, "Snacks", 50, 0xFFFFD54F), // Amarillo
                createProduct("Leche 1L", 28.0, "Lácteos", 30, 0xFFFFFFFF) // Blanco
            )
            "Veterinaria" -> listOf(
                createProduct("Croquetas Perro 2kg", 180.0, "Alimento", 25, 0xFF8D6E63), // Café
                createProduct("Shampoo Antipulgas", 120.0, "Higiene", 15, 0xFF81C784), // Verde
                createProduct("Correa Reflectante", 90.0, "Accesorios", 10, 0xFFF06292) // Rosa
            )
            "Restaurante", "Cafetería" -> listOf(
                createProduct("Café Americano", 35.0, "Bebidas", 100, 0xFF8D6E63), // Café
                createProduct("Latte", 50.0, "Bebidas", 80, 0xFFFFB74D), // Naranja
                createProduct("Rebanada Pastel", 65.0, "Postres", 15, 0xFFF06292) // Rosa
            )
            else -> listOf(
                createProduct("Producto de Ejemplo", 100.0, "General", 50, 0xFF81C784)
            )
        }

        seedProducts.forEach { product ->
            posRepository.addProduct(product)
        }
    }

    private fun createProduct(
        name: String, price: Double, category: String, stock: Int, colorHex: Long
    ): Product {
        return Product(
            id = UUID.randomUUID().toString(),
            name = name,
            price = price,
            category = category,
            stock = stock,
            colorHex = colorHex
        )
    }
}
