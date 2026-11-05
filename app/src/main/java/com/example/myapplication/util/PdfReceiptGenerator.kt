package com.example.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.myapplication.model.BusinessSettings
import com.example.myapplication.model.Order
import com.example.myapplication.model.ServiceType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfReceiptGenerator {

    fun generateAndOpenPdf(
        context: Context,
        order: Order,
        settings: BusinessSettings
    ): File? {
        val pageWidth = if (settings.paperSize == "80mm") 576 else 384
        val pageHeight = 1000 + (order.items.size * 50)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        var y = 40f
        val startX = 20f
        val endX = pageWidth.toFloat() - 20f

        // Cabecera Comercial
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(settings.businessName.uppercase(), pageWidth / 2f, y, paint)
        y += 24f

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText(settings.address, pageWidth / 2f, y, paint)
        y += 18f
        canvas.drawText("RFC: ${settings.taxId} | Tel: ${settings.phone}", pageWidth / 2f, y, paint)
        y += 24f

        // Separador
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("----------------------------------------------------------------", startX, y, paint)
        y += 20f

        // Metadatos de la Orden
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(order.date)
        val serviceText = when (order.serviceType) {
            ServiceType.DINE_IN -> "EN SITIO"
            ServiceType.TAKE_AWAY -> "PARA LLEVAR"
            ServiceType.DELIVERY -> "A DOMICILIO"
        }

        drawRowText(canvas, paint, "Fecha:", formattedDate, startX, endX, y)
        y += 18f
        drawRowText(canvas, paint, "Orden #:", order.id.takeLast(6), startX, endX, y)
        y += 18f
        drawRowText(canvas, paint, "Servicio:", serviceText, startX, endX, y)
        y += 18f
        if (order.customerName != null) {
            drawRowText(canvas, paint, "Cliente:", order.customerName, startX, endX, y)
            y += 18f
        }
        drawRowText(canvas, paint, "Pago:", order.paymentMethod?.name ?: "EFECTIVO", startX, endX, y)
        y += 24f

        // Separador
        canvas.drawText("----------------------------------------------------------------", startX, y, paint)
        y += 20f

        // Lista de Artículos
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        canvas.drawText("ARTÍCULO", startX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("IMPORTE", endX, y, paint)
        y += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)

        order.items.forEach { item ->
            val itemTitle = "${item.quantity}x ${item.product.name}"
            val itemTotalText = "\$${String.format(Locale.getDefault(), "%.2f", item.total)}"
            drawRowText(canvas, paint, itemTitle, itemTotalText, startX, endX, y)
            y += 18f

            if (item.selectedVariant != null || item.selectedModifiers.isNotEmpty()) {
                val variantText = item.selectedVariant?.name ?: ""
                val modifiersText = item.selectedModifiers.joinToString(", ") { it.name }
                val detailsText = listOf(variantText, modifiersText).filter { it.isNotEmpty() }.joinToString(" • ")
                paint.textSize = 10f
                canvas.drawText("   + $detailsText", startX, y, paint)
                paint.textSize = 12f
                y += 16f
            }
        }

        y += 10f
        canvas.drawText("----------------------------------------------------------------", startX, y, paint)
        y += 20f

        // Desglose de Totales
        drawRowText(canvas, paint, "SUBTOTAL", "\$${String.format(Locale.getDefault(), "%.2f", order.subtotal)}", startX, endX, y)
        y += 18f

        if (order.discount > 0) {
            drawRowText(canvas, paint, "DESCUENTO", "-\$${String.format(Locale.getDefault(), "%.2f", order.discount)}", startX, endX, y)
            y += 18f
        }

        drawRowText(canvas, paint, "IVA (16%)", "\$${String.format(Locale.getDefault(), "%.2f", order.tax)}", startX, endX, y)
        y += 18f

        if (order.tip > 0) {
            drawRowText(canvas, paint, "PROPINA", "+\$${String.format(Locale.getDefault(), "%.2f", order.tip)}", startX, endX, y)
            y += 18f
        }

        y += 8f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 16f
        drawRowText(canvas, paint, "TOTAL", "\$${String.format(Locale.getDefault(), "%.2f", order.total)}", startX, endX, y)
        y += 32f

        // Pie de Página
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(settings.footerNote, pageWidth / 2f, y, paint)

        document.finishPage(page)

        // Guardar Archivo PDF
        val pdfFile = File(context.cacheDir, "nova_pos_receipt_${order.id.takeLast(6)}.pdf")
        try {
            document.writeTo(FileOutputStream(pdfFile))
            document.close()

            // Abrir visor de PDF mediante FileProvider
            val pdfUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, "Abrir Recibo PDF"))
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }

    private fun drawRowText(
        canvas: android.graphics.Canvas,
        paint: Paint,
        leftText: String,
        rightText: String,
        startX: Float,
        endX: Float,
        y: Float
    ) {
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(leftText, startX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(rightText, endX, y, paint)
        paint.textAlign = Paint.Align.LEFT
    }
}