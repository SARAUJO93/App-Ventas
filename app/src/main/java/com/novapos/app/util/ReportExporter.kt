package com.novapos.app.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.novapos.app.model.Order
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object ReportExporter {

    fun exportOrdersToCsv(context: Context, orders: List<Order>) {
        val fileName = "Reporte_Ventas_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(System.currentTimeMillis())}.csv"
        val file = File(context.cacheDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                val header = "Folio,Fecha,Metodo_Pago,Tipo_Servicio,Cliente,Subtotal,Descuento,IVA,Propina,Total\n"
                out.write(header.toByteArray())

                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                for (order in orders) {
                    val dateStr = dateFormat.format(order.date)
                    val method = order.paymentMethod?.name ?: "EFECTIVO"
                    val service = order.serviceType.name
                    val customer = order.customerName ?: "General"

                    val line = "${order.id.takeLast(6)},$dateStr,$method,$service,$customer,${order.subtotal},${order.discount},${order.tax},${order.tip},${order.total}\n"
                    out.write(line.toByteArray())
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte de Ventas Nova POS")
                putExtra(Intent.EXTRA_TEXT, "Adjunto reporte de ventas en formato CSV/Excel.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Exportar Reporte a..."))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
