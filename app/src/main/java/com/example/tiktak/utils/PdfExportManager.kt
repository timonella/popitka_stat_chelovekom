package com.example.tiktak.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tiktak.domain.model.DiaryEntry
import com.example.tiktak.domain.model.Emotion
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.io.image.ImageDataFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExportManager(private val context: Context) {

    companion object {
        private const val PDF_DIRECTORY = "exports"
    }

    suspend fun exportEntriesToPdf(
        entries: List<DiaryEntry>,
        fileName: String = "diary_export_${System.currentTimeMillis()}.pdf"
    ): Result<Uri> {
        return try {
            // Создаем директорию для экспорта
            val exportDir = File(context.getExternalFilesDir(null), PDF_DIRECTORY)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val pdfFile = File(exportDir, fileName)
            val pdfWriter = PdfWriter(pdfFile)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            // Заголовок
            addTitle(document)

            // Информация о экспорте
            addExportInfo(document)

            // Записи
            for ((index, entry) in entries.withIndex()) {
                addEntryToDocument(document, entry, index + 1)

                // Добавляем разделитель между записями (кроме последней)
                if (index < entries.size - 1) {
                    addSeparator(document)
                }
            }

            document.close()
            pdfDocument.close()

            // Получаем URI для sharing
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun addTitle(document: Document) {
        val title = Paragraph("Мой дневник")
            .setFontSize(24f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(title)
    }

    private fun addExportInfo(document: Document) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        val infoText = Paragraph()
            .add(Text("Дата экспорта: ${dateFormat.format(Date())}\n")
                .setFontSize(10f))
            .add(Text("Приложение: Дневник Эмоций v1.0\n")
                .setFontSize(10f))
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(20f)
        document.add(infoText)

        // Горизонтальная линия
        addHorizontalLine(document)
    }

    private fun addEntryToDocument(document: Document, entry: DiaryEntry, number: Int) {
        // Номер записи
        val entryNumber = Paragraph("Запись #$number")
            .setFontSize(16f)
            .setBold()
            .setMarginTop(20f)
            .setMarginBottom(10f)
        document.add(entryNumber)

        // Дата
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("ru"))
        val dateText = Paragraph("📅 ${dateFormat.format(entry.createdAt)}")
            .setFontSize(12f)
            .setMarginBottom(5f)
        document.add(dateText)

        // Эмоция
        val emotionText = Paragraph("😊 Эмоция: ${getEmotionDisplayName(entry.emotion)} ${entry.emotion.emoji}")
            .setFontSize(12f)
            .setMarginBottom(5f)
        document.add(emotionText)

        // Заголовок
        val titleText = Paragraph("📌 ${entry.title}")
            .setFontSize(14f)
            .setBold()
            .setMarginTop(5f)
            .setMarginBottom(5f)
        document.add(titleText)

        // Содержание
        val contentText = Paragraph(entry.content)
            .setFontSize(12f)
            .setMarginBottom(10f)
        document.add(contentText)

        // Информация о вложениях
        val hasImages = entry.images.isNotEmpty()
        val hasVideos = entry.videos.isNotEmpty()
        val hasAudio = entry.audioFiles.isNotEmpty()
        val hasDocuments = entry.documents.isNotEmpty()

        if (hasImages || hasVideos || hasAudio || hasDocuments) {
            val attachments = mutableListOf<String>()
            if (hasImages) attachments.add("📷 Фото (${entry.images.size})")
            if (hasVideos) attachments.add("🎬 Видео (${entry.videos.size})")
            if (hasAudio) attachments.add("🎵 Аудио (${entry.audioFiles.size})")
            if (hasDocuments) attachments.add("📄 Документы (${entry.documents.size})")

            val attachmentsText = Paragraph("Вложения: ${attachments.joinToString(", ")}")
                .setFontSize(10f)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setMarginBottom(10f)
            document.add(attachmentsText)
        }

        // Теги
        if (entry.tags.isNotEmpty()) {
            val tagsText = Paragraph("Теги: ${entry.tags.joinToString(", ")}")
                .setFontSize(10f)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
            document.add(tagsText)
        }
    }

    private fun addHorizontalLine(document: Document) {
        val line = Paragraph("─".repeat(80))
            .setFontSize(8f)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10f)
            .setMarginBottom(10f)
        document.add(line)
    }

    private fun addSeparator(document: Document) {
        val separator = Paragraph("◆".repeat(20))
            .setFontSize(8f)
            .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20f)
            .setMarginBottom(10f)
        document.add(separator)
    }

    private fun getEmotionDisplayName(emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "Счастье"
            Emotion.SAD -> "Грусть"
            Emotion.ANGRY -> "Злость"
            Emotion.CALM -> "Спокойствие"
            Emotion.EXCITED -> "Восторг"
            Emotion.TIRED -> "Усталость"
            Emotion.GRATEFUL -> "Благодарность"
            Emotion.LOVED -> "Любовь"
            Emotion.WORRIED -> "Тревога"
            Emotion.NORMAL -> "Нормально"
        }
    }
}