package com.novapos.app.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.novapos.app.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncManager @Inject constructor() {

    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val businessUid: String?
        get() = auth.currentUser?.uid

    suspend fun syncOrderToCloud(order: Order): Boolean {
        val uid = businessUid ?: return false

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

        return try {
            firestore.collection("businesses")
                .document(uid)
                .collection("orders")
                .document(order.id)
                .set(orderMap)
                .await()
            Log.d("FirestoreSyncManager", "Orden ${order.id} sincronizada en la nube con éxito")
            true
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error al sincronizar orden ${order.id}", e)
            false
        }
    }

    suspend fun syncProductToCloud(product: Product): Boolean {
        val uid = businessUid ?: return false

        val productMap = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "price" to product.price,
            "category" to product.category,
            "colorHex" to product.colorHex,
            "barcode" to product.barcode
        )

        return try {
            firestore.collection("businesses")
                .document(uid)
                .collection("products")
                .document(product.id)
                .set(productMap, SetOptions.merge())
                .await()
            Log.d("FirestoreSyncManager", "Producto ${product.id} sincronizado en la nube")
            true
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error al sincronizar producto ${product.id}", e)
            false
        }
    }

    suspend fun deductStockAtomicInCloud(productId: String, quantityToDeduct: Int): Boolean {
        val uid = businessUid ?: return false

        return try {
            firestore.collection("businesses")
                .document(uid)
                .collection("products")
                .document(productId)
                .update("stock", FieldValue.increment(-quantityToDeduct.toLong()))
                .await()
            Log.d("FirestoreSyncManager", "Deducción atómica de stock para $productId (-$quantityToDeduct)")
            true
        } catch (e: Exception) {
            Log.e("FirestoreSyncManager", "Error al deducir stock atómicamente para $productId", e)
            false
        }
    }

    fun listenRemoteProducts(): Flow<List<Product>> = callbackFlow {
        val uid = businessUid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = firestore.collection("businesses")
            .document(uid)
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