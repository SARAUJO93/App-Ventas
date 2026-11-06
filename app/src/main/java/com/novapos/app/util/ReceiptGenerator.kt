package com.novapos.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.novapos.app.model.Order
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object ReceiptGenerator {

    fun generateAndShareReceipt(
        context: Context,
        order: Order,
        businessName: String = "NOVA POS"
    ) {
        val pdfDocument = PdfDocument()

        // Dimensiones estándar para ticket de 80mm
        val pageWidth = 384 // ~80mm en puntos de impresión
        val pageHeight = 600 + (order.items.size * 30) // Dinámico según la cantidad de items

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }

        var y = 40f

        // 1. Encabezado del Negocio
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText(businessName.uppercase(), (pageWidth / 2).toFloat(), y, paint)

        y += 24f
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Comprobante de Venta", (pageWidth / 2).toFloat(), y, paint)

        y += 20f
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(order.date)
        canvas.drawText(dateStr, (pageWidth / 2).toFloat(), y, paint)

        y += 18f
        canvas.drawText("Folio: #${order.id.takeLast(6)}", (pageWidth / 2).toFloat(), y, paint)

        // Línea divisoria
        y += 15f
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, paint)

        // 2. Encabezado de Tabla de Items
        y += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Cant.", 20f, y, paint)
        canvas.drawText("Producto", 70f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total", (pageWidth - 20).toFloat(), y, paint)

        y += 10f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, paint)

        // 3. Desglose de Productos
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 20f

        for (item in order.items) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${item.quantity}x", 20f, y, paint)

            val name = if (item.product.name.length > 18) item.product.name.take(16) + ".." else item.product.name
            canvas.drawText(name, 70f, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$${String.format(Locale.US, "%.2f", item.total)}", (pageWidth - 20).toFloat(), y, paint)

            y += 22f
        }

        // Línea divisoria
        y += 5f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, paint)

        // 4. Totales
        y += 20f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Subtotal:", 140f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%.2f", order.subtotal)}", (pageWidth - 20).toFloat(), y, paint)

        if (order.discount > 0) {
            y += 18f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Descuento:", 140f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-$${String.format(Locale.US, "%.2f", order.discount)}", (pageWidth - 20).toFloat(), y, paint)
        }

        y += 18f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("IVA (16%):", 140f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%.2f", order.tax)}", (pageWidth - 20).toFloat(), y, paint)

        if (order.tip > 0) {
            y += 18f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Propina:", 140f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$${String.format(Locale.US, "%.2f", order.tip)}", (pageWidth - 20).toFloat(), y, paint)
        }

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL:", 140f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%.2f", order.total)}", (pageWidth - 20).toFloat(), y, paint)

        // Línea divisoria final
        y += 15f
        paint.strokeWidth = 1f
        canvas.drawLine(20f, y, (pageWidth - 20).toFloat(), y, paint)

        // Pie de Página
        y += 25f
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("¡Gracias por su compra!", (pageWidth / 2).toFloat(), y, paint)
        y += 18f
        canvas.drawText("Emitido por Nova POS", (pageWidth / 2).toFloat(), y, paint)

        pdfDocument.finishPage(page)

        // Guardar en almacenamiento privado temporal de la app
        val file = File(context.cacheDir, "Ticket_${order.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // Crear URI con FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Abrir Intent de compartir (WhatsApp, Gmail, Bluetooth)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ticket de compra #${order.id.takeLast(6)}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto tu ticket de compra de $businessName. ¡Gracias por tu preferencia!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Enviar ticket por..."))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
