package com.paddle.ocr.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.paddle.ocr.R
import com.paddle.ocr.data.model.PresetSample
import com.paddle.ocr.databinding.ItemPresetSampleBinding
import com.paddle.ocr.utils.SampleImageGenerator

class PresetSampleAdapter(
    private val samples: List<PresetSample>,
    private val onSampleClicked: (PresetSample) -> Unit
) : RecyclerView.Adapter<PresetSampleAdapter.ViewHolder>() {

    var selectedIndex = 0

    inner class ViewHolder(val binding: ItemPresetSampleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPresetSampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = samples.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = samples[position]
        val isSelected = position == selectedIndex

        holder.binding.tvPresetTitle.text = item.title
        holder.binding.tvTag.text = item.tag

        // Generate thumbnail preview
        val thumbnail = SampleImageGenerator.generateSampleBitmap(item.id)
        holder.binding.ivThumbnail.setImageBitmap(thumbnail)

        if (isSelected) {
            holder.binding.cardPreset.strokeColor = holder.itemView.context.getColor(R.color.primary)
            holder.binding.cardPreset.strokeWidth = 4
            holder.binding.cardPreset.setCardBackgroundColor(Color.parseColor("#F0F5FF"))
        } else {
            holder.binding.cardPreset.strokeColor = holder.itemView.context.getColor(R.color.border_light)
            holder.binding.cardPreset.strokeWidth = 2
            holder.binding.cardPreset.setCardBackgroundColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            val prev = selectedIndex
            selectedIndex = holder.adapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedIndex)
            onSampleClicked(item)
        }
    }
}
