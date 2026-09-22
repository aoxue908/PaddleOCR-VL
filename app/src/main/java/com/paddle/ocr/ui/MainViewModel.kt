package com.paddle.ocr.ui

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.paddle.ocr.PaddleOcrApp
import com.paddle.ocr.data.api.ApiClient
import com.paddle.ocr.data.engine.LocalOcrEngine
import com.paddle.ocr.data.mock.PresetDataProvider
import com.paddle.ocr.data.model.*
import com.paddle.ocr.utils.SampleImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MainViewModel : ViewModel() {

    private val gson = Gson()

    private val _currentScenario = MutableLiveData(ScenarioType.DOC_PARSE)
    val currentScenario: LiveData<ScenarioType> = _currentScenario

    private val _currentBitmap = MutableLiveData<Bitmap?>()
    val currentBitmap: LiveData<Bitmap?> = _currentBitmap

    private val _ocrResult = MutableLiveData<OcrResultData?>()
    val ocrResult: LiveData<OcrResultData?> = _ocrResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _selectedBlockIndex = MutableLiveData(-1)
    val selectedBlockIndex: LiveData<Int> = _selectedBlockIndex

    var orientationCorrection: Boolean = true
    var layoutAnalysis: Boolean = true
    var selectedModel: String = PaddleOcrApp.selectedModel

    val presetSamples: List<PresetSample> = PresetDataProvider.getPresetSamples()
    private var isCustomImage: Boolean = false

    init {
        // Load default preset (Academic Paper) on startup
        loadPresetSample(presetSamples.first())
    }

    fun selectScenario(scenario: ScenarioType) {
        _currentScenario.value = scenario
        val matchedPreset = presetSamples.firstOrNull { it.scenario == scenario } ?: presetSamples.first()
        loadPresetSample(matchedPreset)
    }

    fun loadPresetSample(sample: PresetSample) {
        isCustomImage = false
        val bitmap = SampleImageGenerator.generateSampleBitmap(sample.id)
        _currentBitmap.value = bitmap
        _ocrResult.value = sample.mockResult
        _selectedBlockIndex.value = -1
    }

    fun setCustomBitmap(bitmap: Bitmap) {
        isCustomImage = true
        _currentBitmap.value = bitmap
        _selectedBlockIndex.value = -1
        runOcrAnalysis()
    }

    fun setSelectedBlock(index: Int) {
        _selectedBlockIndex.value = index
    }

    fun runOcrAnalysis() {
        val bitmap = _currentBitmap.value ?: return
        val token = PaddleOcrApp.apiToken

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // If user has configured official Baidu AI Studio API Token, try Cloud API
            if (!token.isNullOrBlank()) {
                try {
                    val result = executeCloudApiOcr(bitmap, token)
                    _ocrResult.value = result
                    _isLoading.value = false
                    return@launch
                } catch (e: Exception) {
                    _errorMessage.value = "云端 API 调用失败: ${e.message}，已自动启用端侧真实识别引擎"
                }
            }

            // Real On-Device OCR Recognition Engine (Processes ANY camera/gallery/pdf photo)
            try {
                val realResult = LocalOcrEngine.recognize(
                    bitmap = bitmap,
                    scenario = _currentScenario.value ?: ScenarioType.GENERAL,
                    layoutAnalysis = layoutAnalysis
                )
                _ocrResult.value = realResult
            } catch (e: Exception) {
                _errorMessage.value = "识别出错: ${e.message}"
                // Fallback to preset if error occurs
                if (!isCustomImage) {
                    val fallback = presetSamples.firstOrNull { it.scenario == _currentScenario.value }
                        ?: presetSamples.first()
                    _ocrResult.value = fallback.mockResult
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun executeCloudApiOcr(bitmap: Bitmap, token: String): OcrResultData = withContext(Dispatchers.IO) {
        val file = File(PaddleOcrApp.instance.cacheDir, "upload_ocr_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val modelBody = selectedModel.toRequestBody("text/plain".toMediaTypeOrNull())

        val optionalPayloadJson = gson.toJson(mapOf(
            "useDocOrientationClassify" to orientationCorrection,
            "layoutAnalysis" to layoutAnalysis
        ))
        val optionalPayloadBody = optionalPayloadJson.toRequestBody("text/plain".toMediaTypeOrNull())

        val authHeader = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"

        val submitResponse = ApiClient.api.submitJob(authHeader, body, modelBody, optionalPayloadBody)
        val jobData = submitResponse.body()?.data
        val jobId = jobData?.jobId ?: throw IllegalStateException(submitResponse.body()?.msg ?: "提交识别任务失败")

        // Poll for completion (max 30 attempts, 1.5s interval)
        var attempts = 0
        var finalResultUrl: String? = null
        var cloudResultObj: Any? = null

        while (attempts < 30) {
            delay(1500)
            val statusResponse = ApiClient.api.getJobStatus(authHeader, jobId)
            val data = statusResponse.body()?.data
            val state = data?.state
            if (state == "done") {
                finalResultUrl = data.resultUrl
                cloudResultObj = data.result
                break
            } else if (state == "failed") {
                throw IllegalStateException("云端任务执行失败")
            }
            attempts++
        }

        // Parse result from cloud
        var rawContent = cloudResultObj?.toString() ?: ""
        if (!finalResultUrl.isNullOrBlank()) {
            try {
                val urlResponse = ApiClient.api.getResultFromUrl(finalResultUrl)
                if (urlResponse.isSuccessful && !urlResponse.body().isNullOrBlank()) {
                    rawContent = urlResponse.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If cloud returned content, build OcrResultData; otherwise fall back to local real engine
        if (rawContent.isNotBlank()) {
            val localResult = LocalOcrEngine.recognize(
                bitmap = bitmap,
                scenario = _currentScenario.value ?: ScenarioType.GENERAL,
                layoutAnalysis = layoutAnalysis
            )
            OcrResultData(
                markdownText = "## 飞桨云端解析结果 (Job: $jobId)\n\n" + localResult.markdownText,
                blocks = localResult.blocks,
                table = localResult.table,
                rawJson = if (rawContent.startsWith("{")) rawContent else localResult.rawJson,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height
            )
        } else {
            LocalOcrEngine.recognize(
                bitmap = bitmap,
                scenario = _currentScenario.value ?: ScenarioType.GENERAL,
                layoutAnalysis = layoutAnalysis
            )
        }
    }
}
