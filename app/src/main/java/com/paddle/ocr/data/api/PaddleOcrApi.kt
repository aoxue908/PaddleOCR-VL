package com.paddle.ocr.data.api

import com.paddle.ocr.data.model.JobStatusResponse
import com.paddle.ocr.data.model.JobSubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface PaddleOcrApi {

    @Multipart
    @POST("api/v2/ocr/jobs")
    suspend fun submitJob(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("optionalPayload") optionalPayload: RequestBody?
    ): Response<JobSubmitResponse>

    @GET("api/v2/ocr/jobs/{jobId}")
    suspend fun getJobStatus(
        @Header("Authorization") authorization: String,
        @Path("jobId") jobId: String
    ): Response<JobStatusResponse>

    @GET
    suspend fun getResultFromUrl(
        @Url url: String
    ): Response<String>
}
