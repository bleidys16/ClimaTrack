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
import com.example.climatrack.models.Mantenimiento
import com.example.climatrack.models.OrdenInfo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator(private val context: Context) {

    fun generateTechnicalReport(order: OrdenInfo, maintenance: Mantenimiento?): File? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.DKGRAY
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50f

        // Title
        canvas.drawText("CLIMATRACK - REPORTE TÉCNICO", 150f, y, titlePaint)
        y += 40f

        // Order Info
        canvas.drawText("DETALLES DE LA ORDEN", 50f, y, headerPaint)
        y += 20f
        canvas.drawText("Número: ${order.numero}", 50f, y, bodyPaint)
        canvas.drawText("Fecha: ${order.fecha}", 350f, y, bodyPaint)
        y += 20f
        canvas.drawText("Estado: ${order.estado}", 50f, y, bodyPaint)
        canvas.drawText("Tipo: ${order.tipoServicio}", 350f, y, bodyPaint)
        y += 30f

        // Client & Equipment
        canvas.drawText("CLIENTE Y EQUIPO", 50f, y, headerPaint)
        y += 20f
        canvas.drawText("Cliente: ${order.clienteNombre}", 50f, y, bodyPaint)
        y += 20f
        canvas.drawText("Equipo: ${order.equipoMarca} ${order.equipoModelo}", 50f, y, bodyPaint)
        y += 30f

        // Maintenance Details
        if (maintenance != null) {
            canvas.drawText("TRABAJO REALIZADO", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("Diagnóstico:", 50f, y, bodyPaint)
            y += 15f
            drawWrappedText(canvas, maintenance.diagnostico, 60f, y, 500, bodyPaint)
            y += 40f
            canvas.drawText("Trabajo Realizado:", 50f, y, bodyPaint)
            y += 15f
            drawWrappedText(canvas, maintenance.trabajoRealizado, 60f, y, 500, bodyPaint)
            y += 50f
        }

        // Signature
        order.firmaBase64?.let {
            if (it.isNotEmpty()) {
                try {
                    val decodedString = Base64.decode(it, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    if (bitmap != null) {
                        canvas.drawText("FIRMA DE ACEPTACIÓN:", 50f, y, headerPaint)
                        y += 10f
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 100, true)
                        canvas.drawBitmap(scaledBitmap, 50f, y, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reporte_${order.numero}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }

        return file
    }

    fun generateClientReport(order: OrdenInfo, maintenance: Mantenimiento?): File? {
        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply { textSize = 22f; isFakeBoldText = true; color = Color.parseColor("#3A3E6C") }
        val headerPaint = Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.BLACK }
        val bodyPaint = Paint().apply { textSize = 12f }
        val footerPaint = Paint().apply { textSize = 10f; color = Color.GRAY }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 60f

        // Brand Header
        canvas.drawText("CLIMATRACK", 50f, y, titlePaint)
        canvas.drawText("Certificado de Servicio", 350f, y, headerPaint)
        y += 40f

        canvas.drawRect(50f, y, 545f, y + 2f, Paint().apply { color = Color.LTGRAY })
        y += 30f

        // Order Summary
        canvas.drawText("RESUMEN DEL SERVICIO", 50f, y, headerPaint)
        y += 25f
        canvas.drawText("Orden No: ${order.numero}", 50f, y, bodyPaint)
        canvas.drawText("Fecha: ${order.fecha}", 350f, y, bodyPaint)
        y += 20f
        canvas.drawText("Servicio: ${order.tipoServicio}", 50f, y, bodyPaint)
        canvas.drawText("Técnico: ${order.tecnicoNombre ?: "N/A"}", 350f, y, bodyPaint)
        y += 40f

        // Work Summary
        if (maintenance != null) {
            canvas.drawText("DETALLE TÉCNICO", 50f, y, headerPaint)
            y += 20f
            canvas.drawText("Trabajo realizado:", 50f, y, bodyPaint)
            y += 15f
            drawWrappedText(canvas, maintenance.trabajoRealizado, 60f, y, 480, bodyPaint)
            y += 60f
        }

        // Price Section
        canvas.drawRect(50f, y, 545f, y + 50f, Paint().apply { color = Color.parseColor("#F5F5F5") })
        canvas.drawText("TOTAL PAGADO:", 70f, y + 32f, headerPaint)
        val priceText = "$${String.format(java.util.Locale.getDefault(), "%.2f", order.precioServicio)}"
        canvas.drawText(priceText, 400f, y + 32f, titlePaint)
        y += 80f

        // Footer
        canvas.drawText("Gracias por confiar en ClimaTrack.", 50f, y, footerPaint)
        y += 15f
        canvas.drawText("Este documento es un soporte digital de la atención recibida.", 50f, y, footerPaint)

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Certificado_${order.numero}.pdf")
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
}
