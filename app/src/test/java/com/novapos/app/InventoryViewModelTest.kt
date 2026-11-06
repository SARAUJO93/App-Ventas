package com.novapos.app

import com.novapos.app.fakes.FakePosRepository
import com.novapos.app.viewmodel.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePosRepository
    private lateinit var viewModel: InventoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePosRepository()
        viewModel = InventoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addProductSavesProductToRepository() = runTest {
        backgroundScope.launch { viewModel.products.collect {} }

        viewModel.addProduct(
            name = "Refresco Cola",
            price = 20.0,
            category = "Bebidas",
            stock = 50,
            colorHex = 0xFF4A90E2
        )
        testScheduler.advanceUntilIdle()

        val products = viewModel.products.value
        assertEquals(1, products.size)
        assertEquals("Refresco Cola", products[0].name)
        assertEquals(50, products[0].stock)
    }
}