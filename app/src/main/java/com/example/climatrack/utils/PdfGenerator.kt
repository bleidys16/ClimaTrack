package com.example.climatrack.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Base64
import com.example.climatrack.R
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.models.OrdenInfo
import com.example.climatrack.repositories.ServicioRepository
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PdfGenerator(private val context: Context) {

    fun generateTechnicalReport(order: OrdenInfo, maintenance: Mantenimiento?): File? {
        val servicioRepository = ServicioRepository(context)
        val parts = maintenance?.let { servicioRepository.getRepuestosByMantenimiento(it.id) } ?: emptyList()
        
        val techName = order.tecnicoNombre ?: "Técnico ClimaTrack"

        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true; color = Color.parseColor("#3A3E6C") }
        val headerPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.BLACK }
        val bodyPaint = Paint().apply { textSize = 12f; color = Color.BLACK }
        val subTitlePaint = Paint().apply { textSize = 13f; isFakeBoldText = true; color = Color.DKGRAY }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 60f

        // Logo
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_climatrack)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 60, 60, true)
        canvas.drawBitmap(scaledLogo, 50f, 30f, null)

        canvas.drawText("CLIMATRACK S.A.S", 120f, 55f, titlePaint)
        canvas.drawText("Comprobante de Servicio Profesional", 120f, 75f, headerPaint)
        y = 110f

        canvas.drawRect(50f, y, 545f, y + 2f, Paint().apply { color = Color.parseColor("#3A3E6C") })
        y += 30f

        // Basic Info
        canvas.drawText("DATOS DE LA ORDEN", 50f, y, subTitlePaint)
        y += 20f
        canvas.drawText("Orden No: ${order.numero}", 50f, y, bodyPaint)
        canvas.drawText("Fecha: ${order.fecha}", 350f, y, bodyPaint)
        y += 20f
        canvas.drawText("Cliente: ${order.clienteNombre}", 50f, y, bodyPaint)
        canvas.drawText("Técnico: $techName", 350f, y, bodyPaint)
        y += 40f

        // Maintenance Details
        if (maintenance != null) {
            canvas.drawText("DETALLE TÉCNICO", 50f, y, subTitlePaint)
            y += 20f
            canvas.drawText("Diagnóstico:", 50f, y, headerPaint)
            y += 15f
            drawWrappedText(canvas, maintenance.diagnostico, 60f, y, 480, bodyPaint)
            y += 45f
            canvas.drawText("Trabajo realizado:", 50f, y, headerPaint)
            y += 15f
            drawWrappedText(canvas, maintenance.trabajoRealizado, 60f, y, 480, bodyPaint)
            y += 60f
        }

        // Spare Parts Table
        if (parts.isNotEmpty()) {
            canvas.drawText("REPUESTOS UTILIZADOS", 50f, y, subTitlePaint)
            y += 20f
            canvas.drawText("Nombre", 50f, y, headerPaint)
            canvas.drawText("Cant.", 300f, y, headerPaint)
            canvas.drawText("P. Unit", 380f, y, headerPaint)
            canvas.drawText("Subtotal", 480f, y, headerPaint)
            y += 10f
            canvas.drawLine(50f, y, 545f, y, Paint().apply { color = Color.LTGRAY })
            y += 20f

            parts.forEach { part ->
                canvas.drawText(part.repuestoNombre, 50f, y, bodyPaint)
                canvas.drawText(part.cantidad.toString(), 310f, y, bodyPaint)
                canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", part.precio)}", 380f, y, bodyPaint)
                canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", part.precio * part.cantidad)}", 480f, y, bodyPaint)
                y += 20f
            }
            y += 20f
        }

        // Financial Summary
        val totalParts = parts.sumOf { it.precio * it.cantidad }
        val priceMant = order.precioMantenimiento
        
        canvas.drawRect(50f, y, 545f, y + 80f, Paint().apply { color = Color.parseColor("#F5F5F5") })
        y += 25f
        canvas.drawText("Subtotal Mantenimiento:", 70f, y, bodyPaint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", priceMant)}", 450f, y, bodyPaint)
        y += 20f
        canvas.drawText("Subtotal Repuestos:", 70f, y, bodyPaint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", totalParts)}", 450f, y, bodyPaint)
        y += 25f
        canvas.drawText("TOTAL GENERAL:", 70f, y, headerPaint)
        canvas.drawText("$${String.format(Locale.getDefault(), "%.2f", priceMant + totalParts)}", 440f, y, titlePaint)
        y += 60f

        // Signature
        order.firmaBase64?.let {
            if (it.isNotEmpty()) {
                try {
                    val decodedString = Base64.decode(it, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    if (bitmap != null) {
                        canvas.drawText("FIRMA DEL CLIENTE:", 50f, y, headerPaint)
                        y += 10f
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 180, 80, true)
                        canvas.drawBitmap(scaledBitmap, 50f, y, null)
                    }
                } catch (e: Exception) {}
            }
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Comprobante_${order.numero}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            return null
        } finally {
            pdfDocument.close()
        }
        return file
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Int, paint: Paint) {
        val words = text.split(" ")
        var line = ""
        var currentY = y
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                canvas.drawText(line, x, currentY, paint)
                line = word
                currentY += paint.textSize + 5
            } else {
                line = testLine
            }
        }
        canvas.drawText(line, x, currentY, paint)
    }

    fun generateClientReport(order: OrdenInfo, maintenance: Mantenimiento?): File? {
        return generateTechnicalReport(order, maintenance)
    }
}
