package com.grasstouchers.mobie_northstar.data.api

import com.grasstouchers.mobie_northstar.data.model.AiPrediction
import com.grasstouchers.mobie_northstar.data.model.FloodFeatureCollection
import com.grasstouchers.mobie_northstar.data.model.SosReportRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("flood-points")
    suspend fun getFloodPoints(): Response<FloodFeatureCollection>

    @GET("predictions")
    suspend fun getPredictions(): Response<List<AiPrediction>>

    @POST("reports")
    suspend fun submitSosReport(@Body request: SosReportRequest): Response<ResponseBody>

    @POST("chat")
    suspend fun sendAiChat(@Body body: Map<String, String>): Response<ResponseBody>
}
