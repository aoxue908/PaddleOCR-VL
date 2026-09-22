package com.paddle.ocr.data.model

import android.graphics.PointF
import android.graphics.RectF
import com.google.gson.annotations.SerializedName

enum class ScenarioType(val title: String, val subtitle: String, val tag: String) {
    GENERAL("通用文字识别", "多语言高精度文本检测识别", "PP-OCRv4"),
    DOC_PARSE("智能文档解析", "版面分析转Markdown", "PP-DocBee"),
    TABLE("表格识别提取", "有线无线表格结构提取", "SLANet"),
    FORMULA("数学公式识别", "公式提取转LaTeX代码", "PP-Formula"),
    SEAL("印章文本识别", "圆形椭圆红章弯曲文本", "PP-Seal")
}

enum class BlockType(val displayName: String, val colorHex: String) {
    PARAGRAPH("文本", "#2B65EC"),
    TITLE("文档主标题", "#2B65EC"),
    SUBTITLE("副标题", "#2B65EC"),
    TABLE("表格", "#FA8C16"),
    FORMULA("公式", "#13C2C2"),
    SEAL("印章", "#F5222D"),
    FIGURE("图片", "#722ED1"),
    HEADER("页眉", "#8C8C8C"),
    FOOTER("页脚", "#8C8C8C")
}

data class DocumentBlock(
    val id: Int,
    val text: String,
    val type: BlockType,
    val score: Float,
    val bbox: RectF, // Normalized 0.0 - 1.0 or pixel coordinates
    val points: List<PointF> = emptyList()
)

data class TableData(
    val headers: List<String>,
    val rows: List<List<String>>,
    val markdown: String,
    val csv: String
)

data class OcrResultData(
    val markdownText: String,
    val blocks: List<DocumentBlock>,
    val table: TableData? = null,
    val rawJson: String,
    val imageWidth: Int = 1000,
    val imageHeight: Int = 1400
)

data class PresetSample(
    val id: String,
    val scenario: ScenarioType,
    val title: String,
    val subtitle: String,
    val tag: String,
    val mockResult: OcrResultData
)

// Official API Models (Baidu AI Studio PaddleOCR v2 API)
data class JobSubmitResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String?,
    @SerializedName("data") val data: JobData?
)

data class JobData(
    @SerializedName("jobId") val jobId: String?
)

data class JobStatusResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String?,
    @SerializedName("data") val data: JobStatusData?
)

data class JobStatusData(
    @SerializedName("jobId") val jobId: String?,
    @SerializedName("state") val state: String?, // "pending", "running", "done", "failed"
    @SerializedName("resultUrl") val resultUrl: String?,
    @SerializedName("result") val result: Any?
)
