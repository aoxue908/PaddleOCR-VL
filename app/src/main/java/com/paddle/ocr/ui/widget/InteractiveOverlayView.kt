package com.paddle.ocr.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.paddle.ocr.data.model.BlockType
import com.paddle.ocr.data.model.DocumentBlock
import kotlin.math.max
import kotlin.math.min

class InteractiveOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var imageBitmap: Bitmap? = null
    private var blocks: List<DocumentBlock> = emptyList()
    var selectedIndex: Int = -1
        private set

    var showBoxes: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var onBlockSelectedListener: ((block: DocumentBlock?, index: Int) -> Unit)? = null

    // Transformation Matrix
    private val matrix = Matrix()
    private val inverseMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    // Gesture Detectors
    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    // Paints
    private val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val boxFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val selectedBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5.5f
        color = Color.parseColor("#FAAD14")
    }
    private val selectedBoxFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(45, 250, 173, 20)
    }
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1F2329")
    }
    private val labelDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 24f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F1F5F9")
        textSize = 21f
        typeface = Typeface.DEFAULT
    }

    private val tempRect = RectF()
    private val mappedRect = RectF()

    init {
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                matrix.getValues(matrixValues)
                val currentScale = matrixValues[Matrix.MSCALE_X]
                val newScale = currentScale * scaleFactor

                if (newScale in 0.3f..12.0f) {
                    matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                    invalidate()
                }
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                matrix.postTranslate(-distanceX, -distanceY)
                invalidate()
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                handleTap(e.x, e.y)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                matrix.getValues(matrixValues)
                val currentScale = matrixValues[Matrix.MSCALE_X]
                if (currentScale > 1.4f) {
                    fitImageToCenter()
                } else {
                    matrix.postScale(2.2f, 2.2f, e.x, e.y)
                }
                invalidate()
                return true
            }
        })
    }

    fun setData(bitmap: Bitmap?, documentBlocks: List<DocumentBlock>) {
        this.imageBitmap = bitmap
        this.blocks = documentBlocks
        this.selectedIndex = -1
        post {
            fitImageToCenter()
            invalidate()
        }
    }

    fun selectBlock(index: Int) {
        if (index in blocks.indices) {
            selectedIndex = index
            invalidate()
        } else if (index == -1) {
            selectedIndex = -1
            invalidate()
        }
    }

    fun resetZoom() {
        fitImageToCenter()
        invalidate()
    }

    private fun fitImageToCenter() {
        val bmp = imageBitmap ?: return
        if (width == 0 || height == 0) return

        matrix.reset()
        val scaleX = width.toFloat() / bmp.width.toFloat()
        val scaleY = height.toFloat() / bmp.height.toFloat()
        val scale = min(scaleX, scaleY) * 0.98f

        val dx = (width - bmp.width * scale) / 2f
        val dy = (height - bmp.height * scale) / 2f

        matrix.postScale(scale, scale)
        matrix.postTranslate(dx, dy)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitImageToCenter()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var handled = scaleDetector.onTouchEvent(event)
        handled = gestureDetector.onTouchEvent(event) || handled
        return handled || super.onTouchEvent(event)
    }

    private fun handleTap(screenX: Float, screenY: Float) {
        if (blocks.isEmpty()) return

        matrix.invert(inverseMatrix)
        val touchPoints = floatArrayOf(screenX, screenY)
        inverseMatrix.mapPoints(touchPoints)
        val imageX = touchPoints[0]
        val imageY = touchPoints[1]

        var clickedIndex = -1
        // Search in reverse so top-most elements hit first
        for (i in blocks.indices.reversed()) {
            val block = blocks[i]
            if (block.bbox.contains(imageX, imageY)) {
                clickedIndex = i
                break
            }
        }

        selectedIndex = clickedIndex
        invalidate()

        if (clickedIndex != -1) {
            onBlockSelectedListener?.invoke(blocks[clickedIndex], clickedIndex)
        } else {
            onBlockSelectedListener?.invoke(null, -1)
        }
    }

    private fun wrapTextToLines(text: String, paint: Paint, maxLineWidth: Float): List<String> {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return emptyList()

        val resultLines = mutableListOf<String>()
        val paragraphLines = cleanText.split("\n")

        for (paragraph in paragraphLines) {
            val trimmed = paragraph.trim()
            if (trimmed.isEmpty()) continue

            var start = 0
            val len = trimmed.length
            while (start < len) {
                val count = paint.breakText(trimmed, start, len, true, maxLineWidth, null)
                if (count <= 0) {
                    resultLines.add(trimmed.substring(start))
                    break
                }
                resultLines.add(trimmed.substring(start, start + count))
                start += count
            }
        }
        return resultLines
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bmp = imageBitmap ?: return
        canvas.drawBitmap(bmp, matrix, bitmapPaint)

        if (!showBoxes || blocks.isEmpty()) return

        var selectedBlock: DocumentBlock? = null
        val selectedMappedRect = RectF()

        // 1. Draw sleek bounding boxes for all detected regions
        for (i in blocks.indices) {
            val block = blocks[i]
            tempRect.set(block.bbox)
            matrix.mapRect(mappedRect, tempRect)

            if (i == selectedIndex) {
                selectedBlock = block
                selectedMappedRect.set(mappedRect)
                continue // Draw selected block on top later
            }

            val color = Color.parseColor(block.type.colorHex)
            boxPaint.color = color
            boxFillPaint.color = Color.argb(16, Color.red(color), Color.green(color), Color.blue(color))

            // Draw clean rounded outline and subtle transparent tint
            canvas.drawRoundRect(mappedRect, 4f, 4f, boxFillPaint)
            canvas.drawRoundRect(mappedRect, 4f, 4f, boxPaint)
        }

        // 2. Draw Highlight & Floating Tag Card ONLY for currently selected/tapped block
        if (selectedBlock != null) {
            val block = selectedBlock
            val categoryColor = Color.parseColor(block.type.colorHex)

            // Draw prominent highlight border with category color
            selectedBoxPaint.color = categoryColor
            selectedBoxFillPaint.color = Color.argb(45, Color.red(categoryColor), Color.green(categoryColor), Color.blue(categoryColor))
            canvas.drawRoundRect(selectedMappedRect, 6f, 6f, selectedBoxFillPaint)
            canvas.drawRoundRect(selectedMappedRect, 6f, 6f, selectedBoxPaint)

            val headerText = "${block.type.displayName} ${(block.score * 100).toInt()}%"
            val headerWidth = labelTextPaint.measureText(headerText)

            // Max width for text wrapping inside callout card (75% of canvas width)
            val maxAllowedTextWidth = (width * 0.75f).coerceIn(320f, 700f)
            val bodyLines = wrapTextToLines(block.text, labelBodyPaint, maxAllowedTextWidth)

            val lineSpacing = 28f
            val maxBodyLineWidth = bodyLines.maxOfOrNull { labelBodyPaint.measureText(it) } ?: 0f
            val maxContentWidth = maxOf(headerWidth + 30f, maxBodyLineWidth)

            val cardWidth = maxContentWidth + 36f
            val cardHeight = 40f + (if (bodyLines.isNotEmpty()) (bodyLines.size * lineSpacing + 12f) else 0f)

            val cardLeft = selectedMappedRect.left.coerceIn(8f, (width - cardWidth - 8f).coerceAtLeast(8f))
            val cardTop = if (selectedMappedRect.top - cardHeight - 8f >= 0) {
                selectedMappedRect.top - cardHeight - 8f
            } else {
                (selectedMappedRect.bottom + 8f).coerceAtMost((height - cardHeight - 8f).coerceAtLeast(8f))
            }
            val cardRect = RectF(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight)

            // Dark rounded card container with glass effect
            canvas.drawRoundRect(cardRect, 12f, 12f, labelBgPaint)

            // Category color indicator dot
            labelDotPaint.color = categoryColor
            canvas.drawCircle(cardLeft + 16f, cardTop + 20f, 6.5f, labelDotPaint)

            // Category header text
            canvas.drawText(headerText, cardLeft + 30f, cardTop + 26f, labelTextPaint)

            // Render every line of recognized text content in full without truncation
            for ((index, line) in bodyLines.withIndex()) {
                val lineY = cardTop + 48f + (index * lineSpacing)
                canvas.drawText(line, cardLeft + 16f, lineY, labelBodyPaint)
            }
        }
    }
}
