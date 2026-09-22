package com.paddle.ocr.ui.adapter

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paddle.ocr.data.model.BlockType
import com.paddle.ocr.data.model.DocumentBlock
import com.paddle.ocr.databinding.ItemTextBlockBinding

class TextBlockAdapter(
    private var items: List<DocumentBlock> = emptyList(),
    private var sourceBitmap: Bitmap? = null,
    private val onItemClick: (DocumentBlock, Int) -> Unit
) : RecyclerView.Adapter<TextBlockAdapter.ViewHolder>() {

    var selectedIndex = -1

    fun updateData(newItems: List<DocumentBlock>, bitmap: Bitmap? = null) {
        this.items = newItems
        this.sourceBitmap = bitmap
        this.selectedIndex = -1
        notifyDataSetChanged()
    }

    fun selectItem(index: Int) {
        val prev = selectedIndex
        selectedIndex = index
        if (prev in items.indices) notifyItemChanged(prev)
        if (selectedIndex in items.indices) notifyItemChanged(selectedIndex)
    }

    inner class ViewHolder(val binding: ItemTextBlockBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTextBlockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isSelected = position == selectedIndex
        val categoryColor = Color.parseColor(item.type.colorHex)

        // Text content
        if (item.text.isNotBlank()) {
            holder.binding.tvContent.text = item.text
            holder.binding.tvContent.visibility = View.VISIBLE
        } else {
            holder.binding.tvContent.visibility = View.GONE
        }

        // Image crop preview for FIGURE block (1:1 with AI Studio Document Analysis)
        if (item.type == BlockType.FIGURE && sourceBitmap != null && !sourceBitmap!!.isRecycled) {
            val bmp = sourceBitmap!!
            val box = item.bbox
            // Convert normalized coordinates [0..1] or pixel coordinates
            val left = if (box.right <= 1.0f && bmp.width > 1) (box.left * bmp.width).toInt() else box.left.toInt()
            val top = if (box.bottom <= 1.0f && bmp.height > 1) (box.top * bmp.height).toInt() else box.top.toInt()
            val right = if (box.right <= 1.0f && bmp.width > 1) (box.right * bmp.width).toInt() else box.right.toInt()
            val bottom = if (box.bottom <= 1.0f && bmp.height > 1) (box.bottom * bmp.height).toInt() else box.bottom.toInt()

            val cropW = (right - left).coerceIn(1, bmp.width)
            val cropH = (bottom - top).coerceIn(1, bmp.height)
            val cropX = left.coerceIn(0, (bmp.width - cropW).coerceAtLeast(0))
            val cropY = top.coerceIn(0, (bmp.height - cropH).coerceAtLeast(0))

            try {
                val cropped = Bitmap.createBitmap(bmp, cropX, cropY, cropW, cropH)
                holder.binding.ivBlockImage.setImageBitmap(cropped)
                holder.binding.ivBlockImage.visibility = View.VISIBLE
            } catch (e: Exception) {
                holder.binding.ivBlockImage.visibility = View.GONE
            }
        } else {
            holder.binding.ivBlockImage.visibility = View.GONE
        }

        if (isSelected) {
            // Active Focus / Spotlight Mode matching AI Studio
            holder.binding.cardTextBlock.strokeColor = categoryColor
            holder.binding.cardTextBlock.strokeWidth = 3
            holder.binding.cardTextBlock.setCardBackgroundColor(Color.parseColor("#1E293B"))

            holder.binding.tvCategoryBadge.visibility = View.VISIBLE
            holder.binding.tvCategoryBadge.text = item.type.displayName
            holder.binding.tvCategoryBadge.setBackgroundColor(categoryColor)
            holder.binding.tvCategoryBadge.setTextColor(Color.WHITE)

            holder.binding.tvContent.setTextColor(Color.WHITE)
            if (item.type == BlockType.TITLE) {
                holder.binding.tvContent.textSize = 16f
                holder.binding.tvContent.typeface = Typeface.DEFAULT_BOLD
            } else {
                holder.binding.tvContent.textSize = 14f
                holder.binding.tvContent.typeface = Typeface.DEFAULT
            }
        } else {
            // Muted Dimmed Mode for unselected blocks
            holder.binding.cardTextBlock.strokeColor = Color.TRANSPARENT
            holder.binding.cardTextBlock.strokeWidth = 0
            holder.binding.cardTextBlock.setCardBackgroundColor(Color.TRANSPARENT)

            holder.binding.tvCategoryBadge.visibility = View.GONE

            holder.binding.tvContent.setTextColor(Color.parseColor("#94A3B8"))
            if (item.type == BlockType.TITLE) {
                holder.binding.tvContent.textSize = 15f
                holder.binding.tvContent.typeface = Typeface.DEFAULT_BOLD
            } else {
                holder.binding.tvContent.textSize = 13.5f
                holder.binding.tvContent.typeface = Typeface.DEFAULT
            }
        }

        holder.itemView.setOnClickListener {
            selectItem(holder.adapterPosition)
            onItemClick(item, holder.adapterPosition)
        }
    }
}
