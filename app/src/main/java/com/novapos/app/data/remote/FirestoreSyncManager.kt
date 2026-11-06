package com.novapos.app.data.remote

import android.util.Log
import com.novapos.app.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor() {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val businessId: String
        get() {
            val userEmail: String? = auth.currentUser?.email
            return if (!userEmail.isNullOrBlank()) {
                userEmail.replace(".", "_").replace("@", "_at_")
            } else {
                "default_business"
            }
        }

    fun syncOrderToCloud(order: Order) {
        val orderMap = hashMapOf(
            "id" to order.id,
            "subtotal" to order.subtotal,
            "discount" to order.discount,
            "tax" to order.tax,
            "tip" to order.tip,
            "total" to order.total,
            "status" to order.status.name,
            "dateTimestamp" to order.date.time,
            "paymentMethod" to order.paymentMethod?.name,
            "customerId" to order.customerId,
            "customerName" to order.customerName,
            "serviceType" to order.serviceType.name,
            "itemsCount" to order.items.size
        )

        firestore.collection("businesses")
            .document(businessId)
            .collection("orders")
            .document(order.id)
            .set(orderMap)
            .addOnSuccessListener {
                Log.d("FirestoreSyncManager", "Orden ${order.id} sincronizada en la nube con éxito")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSyncManager", "Error al sincronizar orden ${order.id}", e)
            }
    }

    fun syncProductToCloud(product: Product) {
        val productMap = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "price" to product.price,
            "category" to product.category,
            "stock" to product.stock,
            "colorHex" to product.colorHex,
            "barcode" to product.barcode
        )

        firestore.collection("businesses")
            .document(businessId)
            .collection("products")
            .document(product.id)
            .set(productMap)
            .addOnSuccessListener {
                Log.d("FirestoreSyncManager", "Producto ${product.id} sincronizado en la nube")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSyncManager", "Error al sincronizar producto ${product.id}", e)
            }
    }

    fun deductStockAtomicInCloud(productId: String, quantityToDeduct: Int) {
        firestore.collection("businesses")
            .document(businessId)
            .collection("products")
            .document(productId)
            .update("stock", com.google.firebase.firestore.FieldValue.increment(-quantityToDeduct.toLong()))
            .addOnSuccessListener {
                Log.d("FirestoreSyncManager", "Deducción atómica de stock para $productId (-$quantityToDeduct)")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreSyncManager", "Error al deducir stock atómicamente para $productId", e)
            }
    }

    fun listenRemoteProducts(): Flow<List<Product>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("businesses")
            .document(businessId)
            .collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSyncManager", "Error escuchando productos remotos", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        val id = doc.getString("id") ?: doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val price = doc.getDouble("price") ?: 0.0
                        val category = doc.getString("category") ?: "General"
                        val stock = doc.getLong("stock")?.toInt() ?: 0
                        val colorHex = doc.getLong("colorHex") ?: 0xFF4A90E2
                        val barcode = doc.getString("barcode")

                        Product(
                            id = id,
                            name = name,
                            price = price,
                            category = category,
                            stock = stock,
                            colorHex = colorHex,
                            barcode = barcode
                        )
                    }
                    trySend(products)
                }
            }

        awaitClose { listener.remove() }
    }
}
