package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.TableItemEntity
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.model.Product
import com.example.myapplication.model.Table
import com.example.myapplication.model.TableStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TablesViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    val tables: StateFlow<List<Table>> = repository.getTables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTableItems(tableId: String): Flow<List<TableItemEntity>> {
        return repository.getTableItems(tableId)
    }

    fun updateTableStatus(table: Table, newStatus: TableStatus) {
        viewModelScope.launch {
            repository.updateTable(table.copy(status = newStatus))
        }
    }

    fun addItemToTable(tableId: String, product: Product) {
        viewModelScope.launch {
            repository.addItemToTable(tableId, product)
        }
    }

    fun removeItemFromTable(tableId: String, productId: String) {
        viewModelScope.launch {
            repository.removeItemFromTable(tableId, productId)
        }
    }

    fun clearTableOrder(tableId: String) {
        viewModelScope.launch {
            repository.clearTableOrder(tableId)
        }
    }
}