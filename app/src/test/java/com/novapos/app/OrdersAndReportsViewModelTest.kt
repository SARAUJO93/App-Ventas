package com.novapos.app

import com.novapos.app.fakes.FakePosRepository
import com.novapos.app.viewmodel.OrdersAndReportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrdersAndReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePosRepository
    private lateinit var viewModel: OrdersAndReportsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePosRepository()
        viewModel = OrdersAndReportsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun ordersInitiallyEmptyInFakeRepository() = runTest {
        testScheduler.advanceUntilIdle()
        val orders = viewModel.orders.value
        assertTrue(orders.isEmpty())
    }
}