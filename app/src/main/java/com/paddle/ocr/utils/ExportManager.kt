package com.paddle.ocr.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.paddle.ocr.data.model.DocumentBlock
import java.io.File
import java.io.FileOutputStream

object ExportManager {

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
    }

    private fun getLocalExportDir(context: Context): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val exportDir = File(downloadsDir, "PaddleOCR")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        if (exportDir.exists() && exportDir.canWrite()) {
            return exportDir
        }
        val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val fallbackDir = File(appDownloadsDir, "PaddleOCR")
        if (!fallbackDir.exists()) fallbackDir.mkdirs()
        return fallbackDir
    }

    fun saveTextFileToLocal(context: Context, filename: String, content: String): File? {
        return try {
            val dir = getLocalExportDir(context)
            val outFile = File(dir, filename)
            FileOutputStream(outFile).use { it.write(content.toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "已成功保存至本地文件夹:\n${outFile.absolutePath}", Toast.LENGTH_LONG).show()
            outFile
        } catch (e: Exception) {
            Toast.makeText(context, "保存本地失败: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun saveAnnotatedImageToLocal(context: Context, originalBitmap: Bitmap, blocks: List<DocumentBlock>): File? {
        return try {
            val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(resultBitmap)

            val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }
            val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
            }

            for (block in blocks) {
                val color = Color.parseColor(block.type.colorHex)
                boxPaint.color = color
                canvas.drawRect(block.bbox, boxPaint)

                val label = "${block.type.displayName} ${(block.score * 100).toInt()}%"
                val textW = textPaint.measureText(label)
                val pillRect = RectF(block.bbox.left, block.bbox.top - 30f, block.bbox.left + textW + 16f, block.bbox.top)
                labelBgPaint.color = color
                canvas.drawRoundRect(pillRect, 4f, 4f, labelBgPaint)
                canvas.drawText(label, block.bbox.left + 8f, block.bbox.top - 8f, textPaint)
            }

            val dir = getLocalExportDir(context)
            val imageFile = File(dir, "paddle_ocr_annotated_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use {
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Toast.makeText(context, "标注图片已成功保存到本地:\n${imageFile.absolutePath}", Toast.LENGTH_LONG).show()
            imageFile
        } catch (e: Exception) {
            Toast.makeText(context, "保存标注图失败: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun shareOcrResult(context: Context, content: String, filename: String = "paddle_ocr_result.md") {
        try {
            val cacheFile = File(context.cacheDir, filename)
            FileOutputStream(cacheFile).use { it.write(content.toByteArray(Charsets.UTF_8)) }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_TITLE, filename)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "分享识别结果"))
        } catch (e: Exception) {
            Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
