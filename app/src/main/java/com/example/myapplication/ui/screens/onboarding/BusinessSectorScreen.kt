package com.example.myapplication.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.theme.AppleBlue

data class SectorItem(val name: String, val emoji: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSectorScreen(
    selectedSector: String,
    onSectorSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val sectors = listOf(
        SectorItem("Restaurante", "🍽️"),
        SectorItem("Cafetería", "☕"),
        SectorItem("Abarrotes", "🛒"),
        SectorItem("Tienda de objetos", "📦"),
        SectorItem("Electrónica", "💻"),
        SectorItem("Tienda de ropa", "👗"),
        SectorItem("Farmacia", "💊"),
        SectorItem("Ferretería", "🛠️"),
        SectorItem("Tienda de alcohol", "🍷"),
        SectorItem("Distribuidor", "🚚"),
        SectorItem("Panadería", "🥖"),
        SectorItem("Veterinaria", "🐶")
    )

    val filteredSectors = sectors.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp).background(Color(0xFFF3F7FA))) {
                AppleButton(
                    text = "Siguiente",
                    enabled = selectedSector.isNotEmpty(),
                    onClick = onNext,
                    backgroundColor = if (selectedSector.isNotEmpty()) AppleBlue else Color.LightGray
                )
            }
        },
        containerColor = Color(0xFFF3F7FA) // Fondo claro igual a Yimi
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Giro de negocio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Text(
                text = "Con esto adaptaremos el sistema a tus necesidades.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar otro", color = Color(0xFF718096)) },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xFF2C3E50)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2C3E50),
                    unfocusedTextColor = Color(0xFF2C3E50),
                    focusedLabelColor = AppleBlue,
                    unfocusedLabelColor = Color(0xFF718096),
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E0)
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredSectors) { sector ->
                    SectorCard(
                        item = sector,
                        isSelected = selectedSector == sector.name,
                        onClick = { onSectorSelected(sector.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectorCard(
    item: SectorItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppleBlue.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 1.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AppleBlue) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.emoji,
                    fontSize = 48.sp, // Emoji grande simulando render 3D
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) AppleBlue else Color(0xFF2C3E50),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            // Radio button (indicador circular en la esquina)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    modifier = Modifier.size(24.dp),
                    colors = RadioButtonDefaults.colors(selectedColor = AppleBlue)
                )
            }
        }
    }
}