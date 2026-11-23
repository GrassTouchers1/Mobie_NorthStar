package com.grasstouchers.mobie_northstar.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model kết quả dự đoán ngập lụt bằng AI theo thời gian thực (sau 30p & 60p)
 */
data class AiPrediction(
    @SerializedName("point_id") val pointId: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("current_depth_cm") val currentDepthCm: Double = 0.0,
    @SerializedName("threshold_cm") val thresholdCm: Double = 20.0,
    @SerializedName("forecast_30m") val forecast30m: ForecastInterval? = null,
    @SerializedName("forecast_60m") val forecast60m: ForecastInterval? = null,
    @SerializedName("drainage_forecast") val drainageForecast: DrainageForecast? = null,
    @SerializedName("water_trend") val waterTrend: String? = null,
    @SerializedName("estimated_drainage_time") val estimatedDrainageTime: String? = null
)

data class ForecastInterval(
    @SerializedName("predicted_depth_cm") val predictedDepthCm: Double = 0.0,
    @SerializedName("risk_score") val riskScore: Double = 0.0,
    @SerializedName("exceeds_threshold") val exceedsThreshold: Boolean = false
)

data class DrainageForecast(
    @SerializedName("water_trend") val waterTrend: String? = "falling",
    @SerializedName("estimated_drainage_time") val estimatedDrainageTime: String? = "~ 1 giờ"
)
