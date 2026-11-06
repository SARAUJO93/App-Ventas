package com.novapos.app

import com.novapos.app.model.*
import com.novapos.app.viewmodel.CartViewModel
import com.novapos.app.fakes.FakePosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePosRepository
    private lateinit var viewModel: CartViewModel

    private val sampleProduct = Product(
        id = "p1",
        name = "Taco al Pastor",
        price = 25.0,
        category = "Comida",
        stock = 100,
        barcode = "750123456789"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePosRepository()
        viewModel = CartViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addToCart adds product to cart and updates state`() = runTest {
        viewModel.addToCart(sampleProduct)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("p1", state.items[0].product.id)
        assertEquals(1, state.items[0].quantity)
        assertEquals(25.0, state.subtotal, 0.01)
    }

    @Test
    fun `addToCart twice increments quantity`() = runTest {
        viewModel.addToCart(sampleProduct)
        viewModel.addToCart(sampleProduct)

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(2, state.items[0].quantity)
        assertEquals(50.0, state.subtotal, 0.01)
    }

    @Test
    fun `calculations with discount tax and tip are accurate`() = runTest {
        viewModel.addToCart(sampleProduct) // Subtotal = $25.00
        viewModel.setDiscountPercent(10.0)  // 10% de descuento = $2.50. Taxable = $22.50
        viewModel.setTipPercent(10.0)       // 10% de propina = $2.25

        val state = viewModel.uiState.value
        assertEquals(25.0, state.subtotal, 0.01)
        assertEquals(2.5, state.discount, 0.01)
        assertEquals(22.5, state.taxableAmount, 0.01)
        assertEquals(3.6, state.tax, 0.01)     // 16% IVA de $22.50 = $3.60
        assertEquals(2.25, state.tip, 0.01)    // 10% Propina de $22.50 = $2.25
        assertEquals(28.35, state.total, 0.01) // $22.50 + $3.60 + $2.25 = $28.35
    }

    @Test
    fun `updateCartItemQuantity removes item when quantity is zero`() = runTest {
        viewModel.addToCart(sampleProduct)
        viewModel.updateCartItemQuantity("p1", 0)

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(0.0, state.total, 0.01)
    }

    @Test
    fun `clearCart resets all state`() = runTest {
        viewModel.addToCart(sampleProduct)
        viewModel.setDiscountPercent(15.0)
        viewModel.clearCart()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(0.0, state.discountPercent, 0.01)
        assertEquals(0.0, state.total, 0.01)
    }

    @Test
    fun `processCheckout generates order and clears cart`() = runTest {
        viewModel.addToCart(sampleProduct)
        val order = viewModel.processCheckout(PaymentMethod.CASH)

        assertEquals(1, order.items.size)
        assertEquals(25.0, order.subtotal, 0.01)
        assertEquals(PaymentMethod.CASH, order.paymentMethod)

        // El carrito debe haber quedado limpio
        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
    }
}