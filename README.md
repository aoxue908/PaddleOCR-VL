# PaddleOCR Android 工作台 (飞桨星河社区 1:1 复刻版)

本项目是基于百度飞桨 AI Studio 星河社区 **PaddleOCR 智能文档解析与文字识别工作台** (`https://aistudio.baidu.com/paddleocr/task/new`) 进行 **1:1 深度复刻与移动端工程化重构** 的标准 Android 原生工程。

---

## 🌟 核心特性与功能亮点

### 1. 五大核心识别场景覆盖
* 📖 **智能文档解析 (PP-DocBee / PP-StructureV3)**：版面分析、阅读顺序重构、层次结构提取并输出排版优美的 Markdown 文档。
* 📝 **通用文字识别 (PP-OCRv4 / PP-OCRv5)**：多语言高精度文本检测与识别，支持任意角度旋转自动纠偏。
* 📊 **表格识别提取 (SLANet)**：复杂有线表、无线表、合并单元格结构解析，支持表格视图渲染与一键导出 CSV/Excel。
* 📐 **数学公式识别 (PP-Formula)**：行内公式与独立复杂数学公式检测，输出标准 LaTeX 表达式。
* 🔴 **印章识别提取 (PP-Seal)**：圆形、椭圆形红色单位公章检测、五角星定位与弯曲环形文字精准提取。

### 2. 双引擎驱动设计 (Dual-Engine Architecture)
* ☁️ **云端官方大模型 API 引擎**：
  * 支持在设置中配置个人百度飞桨星河社区专属 `Access Token`。
  * 真实调用 `https://paddleocr.aistudio-app.com/api/v2/ocr/jobs` 异步 Job 接口，直连官方最新大模型 (`PaddleOCR-VL-1.6` 等)。
* 📱 **本地离线全真演示引擎 (Built-in Presets)**：
  * 内置学术论文公式、复杂财务损益表、增值税电子发票、带公章合同、双栏中英文文档 5 套高保真图形与标注数据。
  * 无需配置 Token 或在离线无网环境下亦能流畅体验 1:1 的完整工作流与交互。

### 3. 可视化交互式标注画板 (Interactive Canvas)
* 🔍 **多点触控与手势缩放**：支持双指捏合缩放 (0.4x - 6.0x)、平移拖拽、双击自适应对齐。
* 🎨 **彩色 Bounding Box 标注**：
  * 文本段落 (蓝色 `#1890FF`)、标题 (紫色 `#722ED1`)、表格 (橙色 `#FA8C16`)、公式 (青绿 `#13C2C2`)、印章 (红色 `#F5222D`)、插图 (粉紫 `#EB2F96`)。
  * 附带置信度与类别胶囊药丸标签 (如 `公式 99.6%`)。
* 🔄 **双向联动校对 (Bi-directional Highlighting)**：
  * 点击画布中的标注框，高亮框体并驱动下方结果列表自动滚动对焦至对应文本条目；
  * 点击结果列表中的任意文本行，画布自动定位并点亮对应检测框。

### 4. 多维结果检视器 (Multi-View Inspector)
* 📑 **Markdown 渲染视图**：保留标题排版、引用段落、表格与公式语法。
* 📝 **纯文本/段落视图**：按阅读顺序逐行展示，带置信度徽章与坐标编号。
* 📊 **结构化表格视图**：自适应行列排版，支持横向纵向滚动。
* 📦 **结构化 JSON 视图**：暗色 IDE 风格高亮，支持一键复制代码。
* ⚡ **开发者代码生成器**：根据当前所选场景与参数，自动生成 Python (Requests)、cURL、Android (Kotlin/Retrofit) 调用代码片段，并支持飞桨官方 MCP (Model Context Protocol) 协议指引。

### 5. 完备的导出系统
* 一键导出 Markdown (`.md`) 并调用系统分享。
* 一键导出结构化 JSON (`.json`) 文件。
* 一键导出表格数据为 CSV (`.csv`)。
* 保存带高清彩色标注框的合成图片。

---

## 🛠️ 技术栈与架构规范

* **语言**：Kotlin 1.9.22
* **最低支持版本**：Android 7.0 (API 24)
* **目标 SDK**：Android 14 (API 34)
* **构建系统**：Gradle 8.2 + Android Gradle Plugin 8.2.2
* **架构模式**：MVVM (ViewModel + LiveData + Coroutines)
* **网络与序列化**：Retrofit 2 + OkHttp 3 + Gson
* **富文本排版**：Markwon + Table Plugin
* **PDF 解析**：Android 原生 `PdfRenderer` 高保真位图渲染
* **UI 体系**：Material Design 3 (飞桨品牌蓝 `#1890FF`)

---

## 📁 核心目录结构

```text
d:/Android_Projects/PaddleOCR/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/paddle/ocr/
│   │   │   ├── PaddleOcrApp.kt             # Application 全局入口与持久化配置
│   │   │   ├── data/
│   │   │   │   ├── api/                    # Retrofit 接口与 OkHttp 客户端
│   │   │   │   │   ├── PaddleOcrApi.kt
│   │   │   │   │   └── ApiClient.kt
│   │   │   │   ├── model/                  # 任务模型、标注框与场景枚举
│   │   │   │   │   └── OcrModels.kt
│   │   │   │   └── mock/                   # 5大经典场景全真离线数据源
│   │   │   │       └── PresetDataProvider.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt         # 主界面交互控制器
│   │   │   │   ├── MainViewModel.kt        # 业务逻辑与状态管理
│   │   │   │   ├── adapter/                # 预设样例与文本块适配器
│   │   │   │   │   ├── PresetSampleAdapter.kt
│   │   │   │   │   └── TextBlockAdapter.kt
│   │   │   │   └── widget/                 # 自定义交互缩放标注画板
│   │   │   │       └── InteractiveOverlayView.kt
│   │   │   └── utils/
│   │   │       ├── ExportManager.kt        # 多格式文件导出与剪贴板管理
│   │   │       ├── PdfUtils.kt             # 原生 PDF 首页面渲染工具
│   │   │       └── SampleImageGenerator.kt # 样例矢量高保真位图动态绘制器
│   │   └── res/
│   │       ├── drawable/                   # 飞桨科技图标与背景资源
│   │       ├── layout/                     # 仿星河社区工作台布局与对话框
│   │       └── values/                     # colors, strings, themes 规范
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew.bat
```

---

## 🚀 运行与集成步骤

1. 打开 **Android Studio (Hedgehog / Iguana / Jellyfish 或更高版本)**。
2. 选择 **File -> Open**，定位到本工程目录 `d:\Android_Projects\PaddleOCR`。
3. 等待 Gradle 自动完成依赖同步 (Sync Project with Gradle Files)。
4. 连接 Android 实体机或启动 AVD 模拟器，点击 **Run 'app'** 即可立即运行体验！
5. 如需测试飞桨官方真实大模型调用：
   - 访问 [飞桨 AI Studio 任务页面](https://aistudio.baidu.com/paddleocr/task) 获取专属 Access Token；
   - 点击应用右上角 ⚙️ 设置按钮，填入 Token 并保存即可无缝切换为云端真实大模型推理！
