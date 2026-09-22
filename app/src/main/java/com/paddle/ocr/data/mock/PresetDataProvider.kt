package com.paddle.ocr.data.mock

import android.graphics.PointF
import android.graphics.RectF
import com.google.gson.GsonBuilder
import com.paddle.ocr.data.model.*

object PresetDataProvider {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun getPresetSamples(): List<PresetSample> {
        return listOf(
            createPaperSample(),
            createTableSample(),
            createInvoiceSample(),
            createSealSample(),
            createGeneralSample()
        )
    }

    private fun createPaperSample(): PresetSample {
        val blocks = listOf(
            DocumentBlock(
                id = 1,
                text = "PaddleOCR-VL: 多模态文档解析前沿研究",
                type = BlockType.TITLE,
                score = 0.992f,
                bbox = RectF(48f, 55f, 620f, 90f)
            ),
            DocumentBlock(
                id = 2,
                text = "飞桨计算机视觉技术实验室 · 2026",
                type = BlockType.PARAGRAPH,
                score = 0.985f,
                bbox = RectF(48f, 102f, 320f, 125f)
            ),
            DocumentBlock(
                id = 3,
                text = "摘要：本文提出了一种基于超轻量视觉语言架构(VLM)的文档智能解析系统，在多语言OCR、复杂版面重构与数学公式解析中均达到了行业前沿水平。",
                type = BlockType.PARAGRAPH,
                score = 0.988f,
                bbox = RectF(50f, 135f, 750f, 210f)
            ),
            DocumentBlock(
                id = 4,
                text = "1. 引言与架构设计",
                type = BlockType.TITLE,
                score = 0.991f,
                bbox = RectF(48f, 235f, 240f, 265f)
            ),
            DocumentBlock(
                id = 5,
                text = "近年来，复杂版面分析与端到端文档解析面临排版多样与长序列难题。传统的流水线方法分步执行文本检测与识别，往往累积误差。PaddleOCR 采用统一表征学习，直接输出带有结构标签的 Markdown 文本。",
                type = BlockType.PARAGRAPH,
                score = 0.982f,
                bbox = RectF(48f, 275f, 750f, 355f)
            ),
            DocumentBlock(
                id = 6,
                text = "L_{total} = \\lambda_1 L_{det} + \\lambda_2 L_{rec} + \\alpha \\sum_{i=1}^N \\nabla f(x_i)",
                type = BlockType.FORMULA,
                score = 0.996f,
                bbox = RectF(50f, 370f, 750f, 440f)
            ),
            DocumentBlock(
                id = 7,
                text = "2. 实验基准与性能对比",
                type = BlockType.TITLE,
                score = 0.990f,
                bbox = RectF(48f, 470f, 280f, 500f)
            ),
            DocumentBlock(
                id = 8,
                text = "我们在 109 种语言的标准评测集上进行了广泛测试。模型以仅 0.9B 参数量在综合准确率指标上提升了 4.2%。",
                type = BlockType.PARAGRAPH,
                score = 0.986f,
                bbox = RectF(48f, 510f, 720f, 565f)
            ),
            DocumentBlock(
                id = 9,
                text = "图 1: 多模态视觉语言模型端到端解析流程示意图",
                type = BlockType.FIGURE,
                score = 0.978f,
                bbox = RectF(100f, 600f, 700f, 900f)
            ),
            DocumentBlock(
                id = 10,
                text = "Page 1 of 12 · IEEE Transactions on Pattern Analysis",
                type = BlockType.FOOTER,
                score = 0.972f,
                bbox = RectF(220f, 1045f, 600f, 1075f)
            )
        )

        val markdown = """
# PaddleOCR-VL: 多模态文档解析前沿研究

**飞桨计算机视觉技术实验室 · 2026**

> **摘要**：本文提出了一种基于超轻量视觉语言架构(VLM)的文档智能解析系统，在多语言OCR、复杂版面重构与数学公式解析中均达到了行业前沿水平。

## 1. 引言与架构设计
近年来，复杂版面分析与端到端文档解析面临排版多样与长序列难题。传统的流水线方法分步执行文本检测与识别，往往累积误差. PaddleOCR 采用统一表征学习，直接输出带有结构标签的 Markdown 文本。

### 核心公式损失函数：
${'$'}${'$'}L_{total} = \lambda_1 L_{det} + \lambda_2 L_{rec} + \alpha \sum_{i=1}^N \nabla f(x_i)${'$'}${'$'}

## 2. 实验基准与性能对比
我们在 109 种语言的标准评测集上进行了广泛测试。模型以仅 0.9B 参数量在综合准确率指标上提升了 4.2%。

![图 1: 多模态视觉语言模型端到端解析流程示意图](figure_1.png)

---
*Page 1 of 12 · IEEE Transactions on Pattern Analysis*
        """.trimIndent()

        val rawJson = gson.toJson(mapOf(
            "model" to "PaddleOCR-VL-1.6",
            "doc_type" to "Academic_Paper",
            "language" to "zh-CN,en",
            "blocks_count" to blocks.size,
            "blocks" to blocks.map {
                mapOf(
                    "id" to it.id,
                    "type" to it.type.name,
                    "text" to it.text,
                    "confidence" to it.score,
                    "box" to listOf(it.bbox.left, it.bbox.top, it.bbox.right, it.bbox.bottom)
                )
            }
        ))

        return PresetSample(
            id = "preset_paper",
            scenario = ScenarioType.DOC_PARSE,
            title = "学术论文 (含公式/双栏)",
            subtitle = "LaTeX公式+双栏排版",
            tag = "论文公式",
            mockResult = OcrResultData(
                markdownText = markdown,
                blocks = blocks,
                rawJson = rawJson,
                imageWidth = 800,
                imageHeight = 1100
            )
        )
    }

    private fun createTableSample(): PresetSample {
        val headers = listOf("项目名称", "2025Q1", "2025Q2", "2025Q3", "2025Q4")
        val rows = listOf(
            listOf("营业总收入", "12,450.00", "15,820.50", "18,900.20", "22,400.00"),
            listOf("其中：OCR云服务", "5,200.00", "6,800.00", "8,150.00", "10,200.00"),
            listOf("营业总成本", "6,100.00", "7,300.00", "8,400.00", "9,800.00"),
            listOf("研发费用投入", "2,800.00", "3,400.00", "3,900.00", "4,500.00"),
            listOf("销售与管理费用", "1,200.00", "1,500.00", "1,650.00", "1,900.00"),
            listOf("营业利润总额", "2,350.00", "3,620.50", "4,950.20", "6,200.00"),
            listOf("净利润 (扣非)", "1,980.00", "3,050.00", "4,180.00", "5,250.00"),
            listOf("同比增长率 (%)", "+24.5%", "+28.2%", "+33.6%", "+35.8%")
        )

        val csvBuilder = StringBuilder()
        csvBuilder.append(headers.joinToString(",")).append("\n")
        rows.forEach { row ->
            csvBuilder.append(row.joinToString(",")).append("\n")
        }

        val mdTable = """
| 项目名称 | 2025Q1 | 2025Q2 | 2025Q3 | 2025Q4 |
| :--- | :--- | :--- | :--- | :--- |
| **营业总收入** | 12,450.00 | 15,820.50 | 18,900.20 | 22,400.00 |
| **其中：OCR云服务** | 5,200.00 | 6,800.00 | 8,150.00 | 10,200.00 |
| **营业总成本** | 6,100.00 | 7,300.00 | 8,400.00 | 9,800.00 |
| **研发费用投入** | 2,800.00 | 3,400.00 | 3,900.00 | 4,500.00 |
| **销售与管理费用** | 1,200.00 | 1,500.00 | 1,650.00 | 1,900.00 |
| **营业利润总额** | 2,350.00 | 3,620.50 | 4,950.20 | 6,200.00 |
| **净利润 (扣非)** | 1,980.00 | 3,050.00 | 4,180.00 | 5,250.00 |
| **同比增长率 (%)** | +24.5% | +28.2% | +33.6% | +35.8% |
        """.trimIndent()

        val tableData = TableData(headers, rows, mdTable, csvBuilder.toString())

        val blocks = listOf(
            DocumentBlock(1, "2025-2026年度 智能科技业务财务损益表", BlockType.TITLE, 0.995f, RectF(48f, 55f, 600f, 90f)),
            DocumentBlock(2, "编制单位：飞桨人工智能集团  |  货币单位：万元 (RMB)", BlockType.PARAGRAPH, 0.985f, RectF(48f, 102f, 520f, 125f)),
            DocumentBlock(3, "财务损益全景结构化表格", BlockType.TABLE, 0.993f, RectF(50f, 150f, 750f, 735f))
        )

        val rawJson = gson.toJson(mapOf(
            "model" to "SLANet / PP-Table",
            "task" to "table_recognition",
            "table_shape" to listOf(rows.size + 1, headers.size),
            "headers" to headers,
            "rows" to rows
        ))

        return PresetSample(
            id = "preset_table",
            scenario = ScenarioType.TABLE,
            title = "财务报表 (合并表格)",
            subtitle = "复杂表格结构提取",
            tag = "表格提取",
            mockResult = OcrResultData(
                markdownText = "# 2025-2026年度 智能科技业务财务损益表\n\n编制单位：飞桨人工智能集团  |  货币单位：万元 (RMB)\n\n" + mdTable,
                blocks = blocks,
                table = tableData,
                rawJson = rawJson,
                imageWidth = 800,
                imageHeight = 1100
            )
        )
    }

    private fun createInvoiceSample(): PresetSample {
        val blocks = listOf(
            DocumentBlock(1, "增值税电子普通发票", BlockType.TITLE, 0.998f, RectF(240f, 65f, 560f, 115f)),
            DocumentBlock(2, "发票代码: 011002300111", BlockType.PARAGRAPH, 0.994f, RectF(48f, 135f, 260f, 160f)),
            DocumentBlock(3, "发票号码: 89765432", BlockType.PARAGRAPH, 0.996f, RectF(48f, 165f, 240f, 190f)),
            DocumentBlock(4, "开票日期: 2026年03月18日", BlockType.PARAGRAPH, 0.991f, RectF(490f, 135f, 740f, 160f)),
            DocumentBlock(5, "购买方名称: 百度飞桨开发者科技有限公司", BlockType.PARAGRAPH, 0.989f, RectF(65f, 235f, 550f, 260f)),
            DocumentBlock(6, "纳税人识别号: 91110108MA00XXXX7X", BlockType.PARAGRAPH, 0.992f, RectF(65f, 265f, 480f, 290f)),
            DocumentBlock(7, "货物或应税劳务名称: 飞桨 PaddleOCR 算力企业订阅包", BlockType.PARAGRAPH, 0.988f, RectF(65f, 355f, 600f, 380f)),
            DocumentBlock(8, "单价: ￥8,800.00    数量: 1    金额: ￥8,800.00", BlockType.PARAGRAPH, 0.993f, RectF(65f, 435f, 580f, 460f)),
            DocumentBlock(9, "价税合计 (大写): 玖仟叁佰贰拾捌元整", BlockType.PARAGRAPH, 0.995f, RectF(65f, 625f, 450f, 650f)),
            DocumentBlock(10, "(小写) ￥9,328.00", BlockType.PARAGRAPH, 0.997f, RectF(490f, 625f, 680f, 650f))
        )

        val markdown = """
# 增值税电子普通发票 (KIE 键值对提取)

- **发票代码**: `011002300111`
- **发票号码**: `89765432`
- **开票日期**: 2026年03月18日
- **购买方名称**: 百度飞桨开发者科技有限公司
- **购买方税号**: 91110108MA00XXXX7X
- **服务项目**: 飞桨 PaddleOCR 算力企业订阅包 (V4-Ultra)
- **金额**: ￥8,800.00
- **税额 (6%)**: ￥528.00
- **价税合计 (小写)**: **￥9,328.00**
- **价税合计 (大写)**: 玖仟叁佰贰拾捌元整
- **销售方名称**: 百度在线网络技术(北京)有限公司
        """.trimIndent()

        val rawJson = gson.toJson(mapOf(
            "doc_type" to "VAT_Invoice",
            "code" to "011002300111",
            "number" to "89765432",
            "buyer" to "百度飞桨开发者科技有限公司",
            "total_amount" to 9328.00,
            "seller" to "百度在线网络技术(北京)有限公司"
        ))

        return PresetSample(
            id = "preset_invoice",
            scenario = ScenarioType.GENERAL,
            title = "增值税发票 (KIE字段)",
            subtitle = "键值对结构化抽取",
            tag = "发票KIE",
            mockResult = OcrResultData(
                markdownText = markdown,
                blocks = blocks,
                rawJson = rawJson,
                imageWidth = 800,
                imageHeight = 1100
            )
        )
    }

    private fun createSealSample(): PresetSample {
        val blocks = listOf(
            DocumentBlock(1, "人工智能技术服务战略合作协议", BlockType.TITLE, 0.995f, RectF(170f, 65f, 630f, 105f)),
            DocumentBlock(2, "甲方：飞桨星河智能科技研发中心", BlockType.PARAGRAPH, 0.989f, RectF(65f, 135f, 400f, 160f)),
            DocumentBlock(3, "乙方：星河智汇数码科技股份有限公司", BlockType.PARAGRAPH, 0.988f, RectF(65f, 170f, 420f, 195f)),
            DocumentBlock(4, "第一条 合作内容与技术授权", BlockType.TITLE, 0.991f, RectF(65f, 305f, 320f, 330f)),
            DocumentBlock(5, "1. 甲方负责提供最新版本 PaddleOCR-VL 及 PP-StructureV3 算法支持。", BlockType.PARAGRAPH, 0.984f, RectF(65f, 340f, 720f, 365f)),
            DocumentBlock(6, "飞桨星河智能科技研发中心 (合同专用章)", BlockType.SEAL, 0.994f, RectF(160f, 680f, 340f, 860f)),
            DocumentBlock(7, "签署日期：2026年02月20日", BlockType.PARAGRAPH, 0.987f, RectF(65f, 815f, 320f, 840f))
        )

        val markdown = """
# 人工智能技术服务战略合作协议

**甲方**：飞桨星河智能科技研发中心  
**乙方**：星河智汇数码科技股份有限公司  

### 第一条 合作内容与技术授权
1. 甲方负责提供最新版本 PaddleOCR-VL 及 PP-StructureV3 算法支持。
2. 乙方负责在金融合同、医疗单据及自动化流程中进行方案落地集成。

### 第二条 保密义务与知识产权
双方在合作期间接触到的核心算法模型与训练数据均属最高商业机密。

---
**印章识别结果**：
- **印章类型**：圆形单位合同专用章 (红色油墨)
- **印章主体文字**：`飞桨星河智能科技研发中心`
- **中心图案**：标准五角星
- **底部用途标识**：`合同专用章`
- **置信度**：`99.4%`
        """.trimIndent()

        val rawJson = gson.toJson(mapOf(
            "model" to "PP-Seal",
            "seal_detected" to true,
            "seal_info" to mapOf(
                "shape" to "circular",
                "color" to "red",
                "center" to listOf(250, 770),
                "radius" to 90,
                "text" to "飞桨星河智能科技研发中心",
                "sub_text" to "合同专用章",
                "confidence" to 0.994
            )
        ))

        return PresetSample(
            id = "preset_seal",
            scenario = ScenarioType.SEAL,
            title = "带红章合同 (印章提取)",
            subtitle = "弯曲印章文字识别",
            tag = "印章识别",
            mockResult = OcrResultData(
                markdownText = markdown,
                blocks = blocks,
                rawJson = rawJson,
                imageWidth = 800,
                imageHeight = 1100
            )
        )
    }

    private fun createGeneralSample(): PresetSample {
        val blocks = listOf(
            DocumentBlock(1, "PaddleOCR 智能文字识别系统介绍", BlockType.TITLE, 0.996f, RectF(48f, 55f, 600f, 90f)),
            DocumentBlock(2, "发布版本：PP-OCRv4 / PP-OCRv5  ·  多语言全向检测", BlockType.PARAGRAPH, 0.988f, RectF(48f, 102f, 550f, 125f)),
            DocumentBlock(3, "• 80+ 种多语言字符高精度识别 (English, Chinese, Français, Deutsch)", BlockType.PARAGRAPH, 0.991f, RectF(65f, 195f, 740f, 220f)),
            DocumentBlock(4, "• 任意角度旋转纠正与 360° 方向智能分类器", BlockType.PARAGRAPH, 0.992f, RectF(65f, 230f, 580f, 255f)),
            DocumentBlock(5, "• 端侧极速推理：轻量化模型大小仅 4.5MB，移动端流畅运行", BlockType.PARAGRAPH, 0.989f, RectF(65f, 265f, 680f, 290f))
        )

        val markdown = """
# PaddleOCR 智能文字识别系统介绍

**发布版本**：PP-OCRv4 / PP-OCRv5 · 多语言全向检测

PaddleOCR 致力于打造实用型超轻量 OCR 工具库，支持：
* 80+ 种多语言字符高精度识别 (English, Chinese, Français, Deutsch)
* 任意角度旋转纠正与 360° 方向智能分类器
* 端侧极速推理：轻量化模型大小仅 4.5MB，移动端流畅运行

### 典型应用场景：
1. 身份证、驾驶证、银行卡快速录入
2. 货品运单号、车牌号及表计数值识别
3. 纸质文档扫描归档与智能全文检索
        """.trimIndent()

        return PresetSample(
            id = "preset_general",
            scenario = ScenarioType.GENERAL,
            title = "中英文混合排版文档",
            subtitle = "高精度多语言OCR",
            tag = "通用文字",
            mockResult = OcrResultData(
                markdownText = markdown,
                blocks = blocks,
                rawJson = gson.toJson(mapOf("scenario" to "general", "blocks" to blocks)),
                imageWidth = 800,
                imageHeight = 1100
            )
        )
    }
}
