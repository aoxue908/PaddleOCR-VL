package com.paddle.ocr.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.paddle.ocr.PaddleOcrApp
import com.paddle.ocr.R
import com.paddle.ocr.data.model.ScenarioType
import com.paddle.ocr.data.model.TableData
import com.paddle.ocr.databinding.ActivityMainBinding
import com.paddle.ocr.databinding.DialogExportOptionsBinding
import com.paddle.ocr.databinding.ItemScenarioChipBinding
import com.paddle.ocr.ui.adapter.PresetSampleAdapter
import com.paddle.ocr.ui.adapter.TextBlockAdapter
import com.paddle.ocr.utils.ExportManager
import com.paddle.ocr.utils.PdfUtils
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var markwon: Markwon
    private lateinit var textBlockAdapter: TextBlockAdapter

    // Activity Result Launchers
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            viewModel.setCustomBitmap(bitmap)
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bitmap = com.paddle.ocr.utils.ImageUtils.decodeAndCorrectOrientation(this, uri)
            if (bitmap != null) {
                viewModel.setCustomBitmap(bitmap)
            } else {
                Toast.makeText(this, "读取图片失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bitmap = PdfUtils.renderPdfFirstPage(this, uri)
            if (bitmap != null) {
                viewModel.setCustomBitmap(bitmap)
                Toast.makeText(this, "成功导入 PDF 第一页", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "解析 PDF 失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initMarkwon()
        initViews()
        initResultTabs()
        observeViewModel()
    }

    private fun initMarkwon() {
        markwon = Markwon.builder(this)
            .usePlugin(TablePlugin.create(this))
            .build()
    }

    private fun initViews() {
        // App bar
        binding.btnShare.setOnClickListener {
            val result = viewModel.ocrResult.value
            if (result != null) {
                ExportManager.shareOcrResult(this, result.markdownText)
            } else {
                Toast.makeText(this, "暂无识别结果可分享", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnResetTask.setOnClickListener {
            viewModel.loadPresetSample(viewModel.presetSamples.first())
            Toast.makeText(this, "已重置工作台任务", Toast.LENGTH_SHORT).show()
        }

        // Upload triggers
        binding.btnPickCamera.setOnClickListener { takePhotoLauncher.launch(null) }
        binding.btnPickGallery.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnPickPdf.setOnClickListener { pickPdfLauncher.launch("application/pdf") }

        // Configuration
        binding.switchOrientation.setOnCheckedChangeListener { _, isChecked ->
            viewModel.orientationCorrection = isChecked
        }
        binding.switchLayoutAnalysis.setOnCheckedChangeListener { _, isChecked ->
            viewModel.layoutAnalysis = isChecked
        }
        binding.tvModelSelector.setOnClickListener { showModelSelectionDialog() }

        binding.btnRunOcr.setOnClickListener { viewModel.runOcrAnalysis() }

        // Canvas controls
        binding.btnToggleBoxes.setOnClickListener {
            val nextState = !binding.interactiveCanvas.showBoxes
            binding.interactiveCanvas.showBoxes = nextState
            binding.btnToggleBoxes.text = if (nextState) getString(R.string.action_toggle_overlay) else getString(R.string.action_toggle_overlay_show)
        }
        binding.btnFitCanvas.setOnClickListener {
            binding.interactiveCanvas.resetZoom()
        }

        // Bi-directional Canvas tap listener
        binding.interactiveCanvas.onBlockSelectedListener = { block, index ->
            if (index != -1) {
                if (binding.tabLayoutResult.selectedTabPosition != 0) {
                    binding.tabLayoutResult.getTabAt(0)?.select()
                }
                textBlockAdapter.selectItem(index)
                binding.rvTextBlocks.smoothScrollToPosition(index)
            } else {
                textBlockAdapter.selectItem(-1)
            }
        }

        // Bottom Actions
        binding.btnCopyResult.setOnClickListener { copyCurrentTabContent() }
        binding.btnExportOptions.setOnClickListener { showExportDialog() }
    }

    private fun initResultTabs() {
        textBlockAdapter = TextBlockAdapter { _, index ->
            // Highlighting corresponding box in Canvas!
            binding.interactiveCanvas.selectBlock(index)
        }
        binding.rvTextBlocks.adapter = textBlockAdapter

        binding.tabLayoutResult.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showTabContent(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        showTabContent(0)
    }

    private fun showTabContent(position: Int) {
        // Tab 0: 文档解析
        binding.rvTextBlocks.visibility = if (position == 0) View.VISIBLE else View.GONE
        // Tab 1: Markdown 预览
        binding.viewMarkdown.visibility = if (position == 1) View.VISIBLE else View.GONE
        // Tab 2: 结构化表格
        binding.viewTable.visibility = if (position == 2) View.VISIBLE else View.GONE
        // Tab 3: JSON 结构体
        binding.viewJson.visibility = if (position == 3) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.currentBitmap.observe(this) { bitmap ->
            val blocks = viewModel.ocrResult.value?.blocks ?: emptyList()
            binding.interactiveCanvas.setData(bitmap, blocks)
            textBlockAdapter.updateData(blocks, bitmap)
        }

        viewModel.ocrResult.observe(this) { result ->
            if (result != null) {
                val currentBmp = viewModel.currentBitmap.value
                binding.interactiveCanvas.setData(currentBmp, result.blocks)

                // 1. Text Blocks (Document Analysis Spotlight)
                textBlockAdapter.updateData(result.blocks, currentBmp)

                // 2. Markdown
                markwon.setMarkdown(binding.tvMarkdownContent, result.markdownText)

                // 3. Table Layout
                renderTable(result.table)

                // 4. JSON
                binding.tvJsonContent.text = result.rawJson
            }
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnRunOcr.isEnabled = !loading
            binding.btnRunOcr.text = if (loading) getString(R.string.btn_processing) else getString(R.string.btn_start_ocr)
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderTable(tableData: TableData?) {
        binding.tableLayout.removeAllViews()
        if (tableData == null) {
            val emptyTv = TextView(this).apply {
                text = "当前文档未检测到结构化表格数据"
                setTextColor(getColor(R.color.text_secondary))
                setPadding(30, 60, 30, 60)
            }
            binding.tableLayout.addView(emptyTv)
            return
        }

        // Headers
        val headerRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E6F4FF"))
            setPadding(8, 12, 8, 12)
        }
        tableData.headers.forEach { headerText ->
            val cell = TextView(this).apply {
                text = headerText
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(getColor(R.color.text_primary))
                setPadding(16, 12, 16, 12)
                gravity = Gravity.CENTER
            }
            headerRow.addView(cell)
        }
        binding.tableLayout.addView(headerRow)

        // Data Rows
        tableData.rows.forEachIndexed { index, rowList ->
            val dataRow = TableRow(this).apply {
                setBackgroundColor(if (index % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFC"))
                setPadding(8, 10, 8, 10)
            }
            rowList.forEach { cellText ->
                val cell = TextView(this).apply {
                    text = cellText
                    textSize = 12f
                    setTextColor(getColor(R.color.text_primary))
                    setPadding(16, 10, 16, 10)
                    gravity = Gravity.CENTER
                }
                dataRow.addView(cell)
            }
            binding.tableLayout.addView(dataRow)
        }
    }

    private fun copyCurrentTabContent() {
        val result = viewModel.ocrResult.value ?: return
        val currentTab = binding.tabLayoutResult.selectedTabPosition
        when (currentTab) {
            0 -> {
                val allText = result.blocks.joinToString("\n\n") { it.text }
                ExportManager.copyToClipboard(this, "PaddleOCR_DocParse", allText)
            }
            1 -> ExportManager.copyToClipboard(this, "PaddleOCR_Markdown", result.markdownText)
            2 -> ExportManager.copyToClipboard(this, "PaddleOCR_Table", result.table?.csv ?: "无表格数据")
            3 -> ExportManager.copyToClipboard(this, "PaddleOCR_JSON", result.rawJson)
        }
    }

    private fun showExportDialog() {
        val result = viewModel.ocrResult.value ?: run {
            Toast.makeText(this, "暂无识别结果可导出", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogExportOptionsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnExportMarkdown.setOnClickListener {
            ExportManager.saveTextFileToLocal(this, "paddle_ocr_result.md", result.markdownText)
            dialog.dismiss()
        }
        dialogBinding.btnExportJson.setOnClickListener {
            ExportManager.saveTextFileToLocal(this, "paddle_ocr_result.json", result.rawJson)
            dialog.dismiss()
        }
        dialogBinding.btnExportCsv.setOnClickListener {
            val csv = result.table?.csv ?: "无可导出的表格数据"
            ExportManager.saveTextFileToLocal(this, "paddle_ocr_table.csv", csv)
            dialog.dismiss()
        }
        dialogBinding.btnSaveAnnotatedImage.setOnClickListener {
            val bitmap = viewModel.currentBitmap.value
            if (bitmap != null) {
                ExportManager.saveAnnotatedImageToLocal(this, bitmap, result.blocks)
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showModelSelectionDialog() {
        val models = arrayOf("PaddleOCR-VL-1.6 (推荐 - 多模态前沿)", "PP-StructureV3 (复杂文档版面)", "PP-OCRv4 (通用轻量极速)")
        val modelKeys = arrayOf("PaddleOCR-VL-1.6", "PP-StructureV3", "PP-OCRv4")

        AlertDialog.Builder(this)
            .setTitle("选择识别与解析模型")
            .setItems(models) { _, which ->
                viewModel.selectedModel = modelKeys[which]
                binding.tvModelSelector.text = models[which]
                Toast.makeText(this, "已切换模型为: ${modelKeys[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
