package com.novapos.app

import androidx.test.core.app.ApplicationProvider
import com.novapos.app.fakes.FakePosRepository
import com.novapos.app.viewmodel.ShiftViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ShiftViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePosRepository
    private lateinit var viewModel: ShiftViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        repository = FakePosRepository()
        viewModel = ShiftViewModel(context, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginWithPinAuthenticatesDefaultAdminPin() = runTest {
        var isSuccess = false
        viewModel.loginWithPin("1234", onSuccess = { isSuccess = true }, onError = {})
        testScheduler.advanceUntilIdle()

        // Admin PIN "1234" se autentica exitosamente
        assertTrue(isSuccess)
        assertNotNull(viewModel.currentEmployee.value)
        assertEquals("ADMIN", viewModel.currentEmployee.value?.role)
    }

    @Test
    fun logoutEmployeeClearsCurrentEmployee() = runTest {
        viewModel.loginWithPin("1234", onSuccess = {}, onError = {})
        testScheduler.advanceUntilIdle()

        viewModel.logoutEmployee()
        assertNull(viewModel.currentEmployee.value)
    }
}