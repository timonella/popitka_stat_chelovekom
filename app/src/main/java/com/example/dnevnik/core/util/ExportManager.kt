package com.example.dnevnik.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.dnevnik.data.local.entity.JournalEntryEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportToPdf(entries: List<JournalEntryEntity>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        var yPosition = 40f

        paint.textSize = 24f
        canvas.drawText("Дневник эмоций", 40f, yPosition, paint)
        yPosition += 40f

        paint.textSize = 14f
        for (entry in entries) {
            if (yPosition > 800f) {
                document.finishPage(page)
                val newPage = document.startPage(pageInfo)
                val newCanvas = newPage.canvas
                yPosition = 40f

                canvas.drawText("${entry.dayOfWeek} - ${entry.emotion}", 40f, yPosition, paint)
                yPosition += 20f
                canvas.drawText(entry.text, 40f, yPosition, paint)
                yPosition += 30f
            } else {
                canvas.drawText("${entry.dayOfWeek} - ${entry.emotion}", 40f, yPosition, paint)
                yPosition += 20f
                canvas.drawText(entry.text, 40f, yPosition, paint)
                yPosition += 30f
            }
        }

        document.finishPage(page)

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "dnevnik_export_${System.currentTimeMillis()}.pdf"
        )
        document.writeTo(java.io.FileOutputStream(file))
        document.close()

        return file
    }

    fun exportToMarkdown(entries: List<JournalEntryEntity>): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "dnevnik_export_${System.currentTimeMillis()}.md"
        )

        FileWriter(file).use { writer ->
            writer.write("# Дневник эмоций\n\n")
            writer.write("Дата экспорта: ${java.text.SimpleDateFormat("dd.MM.yyyy").format(java.util.Date())}\n\n")
            writer.write("---\n\n")

            for (entry in entries) {
                writer.write("## ${entry.dayOfWeek}\n\n")
                writer.write("**Эмоция:** ${entry.emotion}\n\n")
                if (entry.title.isNotEmpty()) {
                    writer.write("**Заголовок:** ${entry.title}\n\n")
                }
                writer.write("${entry.text}\n\n")
                writer.write("---\n\n")
            }
        }

        return file
    }

    fun exportToJson(entries: List<JournalEntryEntity>): File {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "dnevnik_export_${System.currentTimeMillis()}.json"
        )

        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(entries)

        FileWriter(file).use { writer ->
            writer.write(json)
        }

        return file
    }
}