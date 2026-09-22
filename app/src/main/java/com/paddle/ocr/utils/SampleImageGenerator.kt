package com.paddle.ocr.utils

import android.graphics.*
import com.paddle.ocr.data.model.BlockType
import com.paddle.ocr.data.model.DocumentBlock

object SampleImageGenerator {

    private const val WIDTH = 800
    private const val HEIGHT = 1100

    fun generateSampleBitmap(id: String): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (id) {
            "preset_paper" -> drawPaper(canvas, paint)
            "preset_table" -> drawFinancialTable(canvas, paint)
            "preset_invoice" -> drawInvoice(canvas, paint)
            "preset_seal" -> drawContractWithSeal(canvas, paint)
            else -> drawGeneralDoc(canvas, paint)
        }

        return bitmap
    }

    private fun drawPaper(canvas: Canvas, paint: Paint) {
        // Title
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("PaddleOCR-VL: 多模态文档解析前沿研究", 50f, 80f, paint)

        // Authors & Abstract
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#646A73")
        canvas.drawText("飞桨计算机视觉技术实验室 · 2026", 50f, 115f, paint)

        // Abstract Box
        paint.color = Color.parseColor("#F5F7FA")
        canvas.drawRect(50f, 135f, 750f, 210f, paint)
        paint.color = Color.parseColor("#262626")
        paint.textSize = 13f
        canvas.drawText("摘要：本文提出了一种基于超轻量视觉语言架构(VLM)的文档智能解析系统，", 65f, 165f, paint)
        canvas.drawText("在多语言OCR、复杂版面重构与数学公式解析中均达到了行业前沿水平。", 65f, 190f, paint)

        // Section 1: Introduction
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("1. 引言与架构设计", 50f, 255f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#434343")
        canvas.drawText("近年来，复杂版面分析与端到端文档解析面临排版多样与长序列难题。", 50f, 290f, paint)
        canvas.drawText("传统的流水线方法分步执行文本检测与识别，往往累积误差。", 50f, 315f, paint)
        canvas.drawText("PaddleOCR 采用统一表征学习，直接输出带有结构标签的 Markdown 文本。", 50f, 340f, paint)

        // Math Formula
        paint.color = Color.parseColor("#F9F0FF")
        canvas.drawRect(50f, 370f, 750f, 440f, paint)
        paint.color = Color.parseColor("#722ED1")
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("L_{total} = \\lambda_1 L_{det} + \\lambda_2 L_{rec} + \\alpha \\sum_{i=1}^N \\nabla f(x_i)", 80f, 412f, paint)

        // Section 2: Experiments
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("2. 实验基准与性能对比", 50f, 490f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#434343")
        canvas.drawText("我们在 109 种语言的标准评测集上进行了广泛测试。", 50f, 525f, paint)
        canvas.drawText("模型以仅 0.9B 参数量在综合准确率指标上提升了 4.2%。", 50f, 550f, paint)

        // Simple mock figure
        paint.color = Color.parseColor("#E6F7FF")
        canvas.drawRect(100f, 600f, 700f, 850f, paint)
        paint.color = Color.parseColor("#1890FF")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRect(100f, 600f, 700f, 850f, paint)
        paint.style = Paint.Style.FILL
        paint.textSize = 16f
        canvas.drawText("图 1: 多模态视觉语言模型端到端解析流程示意图", 210f, 885f, paint)

        // Footer
        paint.color = Color.parseColor("#8C8C8C")
        paint.textSize = 12f
        canvas.drawText("Page 1 of 12 · IEEE Transactions on Pattern Analysis", 230f, 1060f, paint)
    }

    private fun drawFinancialTable(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("2025-2026年度 智能科技业务财务损益表", 50f, 80f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#646A73")
        canvas.drawText("编制单位：飞桨人工智能集团  |  货币单位：万元 (RMB)", 50f, 115f, paint)

        // Draw Table Grid
        val startX = 50f
        val startY = 150f
        val rowHeight = 65f
        val colWidths = floatArrayOf(180f, 130f, 130f, 130f, 130f)
        val rows = 9

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#E6F4FF")
        canvas.drawRect(startX, startY, startX + 700f, startY + rowHeight, paint)

        // Outer Border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#1890FF")
        paint.strokeWidth = 2f
        canvas.drawRect(startX, startY, startX + 700f, startY + rowHeight * rows, paint)

        // Inner Grid Lines
        paint.color = Color.parseColor("#D9D9D9")
        paint.strokeWidth = 1f
        for (i in 1 until rows) {
            val y = startY + i * rowHeight
            canvas.drawLine(startX, y, startX + 700f, y, paint)
        }
        var currentX = startX
        for (i in 0 until 4) {
            currentX += colWidths[i]
            canvas.drawLine(currentX, startY, currentX, startY + rowHeight * rows, paint)
        }

        // Table Content
        paint.style = Paint.Style.FILL
        paint.textSize = 14f
        paint.color = Color.parseColor("#1F2329")
        paint.isFakeBoldText = true

        val headers = arrayOf("项目名称", "2025Q1", "2025Q2", "2025Q3", "2025Q4")
        var hX = startX
        for (i in headers.indices) {
            canvas.drawText(headers[i], hX + 20f, startY + 40f, paint)
            hX += colWidths[i]
        }

        paint.isFakeBoldText = false
        val tableRows = arrayOf(
            arrayOf("营业总收入", "12,450.00", "15,820.50", "18,900.20", "22,400.00"),
            arrayOf("其中：OCR云服务", "5,200.00", "6,800.00", "8,150.00", "10,200.00"),
            arrayOf("营业总成本", "6,100.00", "7,300.00", "8,400.00", "9,800.00"),
            arrayOf("研发费用投入", "2,800.00", "3,400.00", "3,900.00", "4,500.00"),
            arrayOf("销售与管理费用", "1,200.00", "1,500.00", "1,650.00", "1,900.00"),
            arrayOf("营业利润总额", "2,350.00", "3,620.50", "4,950.20", "6,200.00"),
            arrayOf("净利润 (扣非)", "1,980.00", "3,050.00", "4,180.00", "5,250.00"),
            arrayOf("同比增长率 (%)", "+24.5%", "+28.2%", "+33.6%", "+35.8%")
        )

        for (r in tableRows.indices) {
            val y = startY + (r + 1) * rowHeight + 40f
            var cX = startX
            for (c in tableRows[r].indices) {
                if (c == 0) paint.isFakeBoldText = true else paint.isFakeBoldText = false
                canvas.drawText(tableRows[r][c], cX + 15f, y, paint)
                cX += colWidths[c]
            }
        }
    }

    private fun drawInvoice(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#8C1D40")
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("增值税电子普通发票", 260f, 90f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawLine(240f, 105f, 560f, 105f, paint)
        canvas.drawLine(240f, 110f, 560f, 110f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#1F2329")

        canvas.drawText("发票代码: 011002300111", 50f, 150f, paint)
        canvas.drawText("发票号码: 89765432", 50f, 180f, paint)
        canvas.drawText("开票日期: 2026年03月18日", 500f, 150f, paint)
        canvas.drawText("校 验 码: 28472 91837 46582 10293", 400f, 180f, paint)

        // Invoice main border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#8C1D40")
        paint.strokeWidth = 2f
        canvas.drawRect(50f, 210f, 750f, 750f, paint)

        // Divider
        canvas.drawLine(50f, 320f, 750f, 320f, paint)
        canvas.drawLine(50f, 600f, 750f, 600f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#1F2329")
        canvas.drawText("购买方名称: 百度飞桨开发者科技有限公司", 70f, 250f, paint)
        canvas.drawText("纳税人识别号: 91110108MA00XXXX7X", 70f, 280f, paint)

        canvas.drawText("货物或应税劳务名称: 飞桨 PaddleOCR 算力企业订阅包", 70f, 370f, paint)
        canvas.drawText("规格型号: V4-Ultra", 70f, 410f, paint)
        canvas.drawText("单价: ￥8,800.00    数量: 1    金额: ￥8,800.00", 70f, 450f, paint)
        canvas.drawText("税率: 6%    税额: ￥528.00", 70f, 490f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("价税合计 (大写): 玖仟叁佰贰拾捌元整", 70f, 640f, paint)
        canvas.drawText("(小写) ￥9,328.00", 500f, 640f, paint)

        paint.isFakeBoldText = false
        canvas.drawText("销售方名称: 百度在线网络技术(北京)有限公司", 70f, 690f, paint)
        canvas.drawText("销售方纳税人识别号: 91110108700000000X", 70f, 720f, paint)
    }

    private fun drawContractWithSeal(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("人工智能技术服务战略合作协议", 180f, 90f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#333333")
        canvas.drawText("甲方：飞桨星河智能科技研发中心", 70f, 150f, paint)
        canvas.drawText("乙方：星河智汇数码科技股份有限公司", 70f, 185f, paint)

        canvas.drawText("鉴于双方在计算机视觉、深度学习以及智能文档解析领域的技术积累，", 70f, 240f, paint)
        canvas.drawText("甲乙双方经友好协商，就共同推进 PaddleOCR 产业落地达成如下协议：", 70f, 270f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("第一条 合作内容与技术授权", 70f, 320f, paint)
        paint.isFakeBoldText = false
        canvas.drawText("1. 甲方负责提供最新版本 PaddleOCR-VL 及 PP-StructureV3 算法支持。", 70f, 355f, paint)
        canvas.drawText("2. 乙方负责在金融合同、医疗单据及自动化流程中进行方案落地集成。", 70f, 385f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("第二条 保密义务与知识产权", 70f, 435f, paint)
        paint.isFakeBoldText = false
        canvas.drawText("双方在合作期间接触到的核心算法模型与训练数据均属最高商业机密。", 70f, 470f, paint)

        canvas.drawText("甲方盖章：___________________", 70f, 750f, paint)
        canvas.drawText("法定代表人签字：张三", 70f, 790f, paint)
        canvas.drawText("签署日期：2026年02月20日", 70f, 830f, paint)

        // Draw Authentic Red Circular Seal
        val sealCenterX = 250f
        val sealCenterY = 770f
        val sealRadius = 90f

        paint.color = Color.parseColor("#D32F2F")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(sealCenterX, sealCenterY, sealRadius, paint)

        // Five-pointed star
        paint.style = Paint.Style.FILL
        drawStar(canvas, sealCenterX, sealCenterY, 22f, paint)

        // Curved text along circle
        val path = Path()
        val rectF = RectF(sealCenterX - 75f, sealCenterY - 75f, sealCenterX + 75f, sealCenterY + 75f)
        path.addArc(rectF, 190f, 160f)
        paint.textSize = 15f
        paint.isFakeBoldText = true
        canvas.drawTextOnPath("飞桨星河智能科技研发中心", path, 0f, 0f, paint)

        // Seal Bottom Tag
        paint.textSize = 13f
        canvas.drawText("合同专用章", sealCenterX - 32f, sealCenterY + 50f, paint)
    }

    private fun drawGeneralDoc(canvas: Canvas, paint: Paint) {
        paint.color = Color.parseColor("#1F2329")
        paint.textSize = 26f
        paint.isFakeBoldText = true
        canvas.drawText("PaddleOCR 智能文字识别系统介绍", 50f, 80f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.parseColor("#646A73")
        canvas.drawText("发布版本：PP-OCRv4 / PP-OCRv5  ·  多语言全向检测", 50f, 115f, paint)

        paint.color = Color.parseColor("#262626")
        canvas.drawText("PaddleOCR 致力于打造实用型超轻量 OCR 工具库，支持：", 50f, 170f, paint)
        canvas.drawText("• 80+ 种多语言字符高精度识别 (English, Chinese, Français, Deutsch)", 70f, 210f, paint)
        canvas.drawText("• 任意角度旋转纠正与 360° 方向智能分类器", 70f, 245f, paint)
        canvas.drawText("• 端侧极速推理：轻量化模型大小仅 4.5MB，移动端流畅运行", 70f, 280f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("典型应用场景：", 50f, 340f, paint)
        paint.isFakeBoldText = false
        canvas.drawText("1. 身份证、驾驶证、银行卡快速录入", 70f, 375f, paint)
        canvas.drawText("2. 货品运单号、车牌号及表计数值识别", 70f, 410f, paint)
        canvas.drawText("3. 纸质文档扫描归档与智能全文检索", 70f, 445f, paint)
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val innerRadius = radius * 0.4f
        var angle = -Math.PI / 2
        val step = Math.PI / 5

        path.moveTo((cx + radius * Math.cos(angle)).toFloat(), (cy + radius * Math.sin(angle)).toFloat())
        for (i in 1..5) {
            angle += step
            path.lineTo((cx + innerRadius * Math.cos(angle)).toFloat(), (cy + innerRadius * Math.sin(angle)).toFloat())
            angle += step
            path.lineTo((cx + radius * Math.cos(angle)).toFloat(), (cy + radius * Math.sin(angle)).toFloat())
        }
        path.close()
        canvas.drawPath(path, paint)
    }
}
