package com.grasstouchers.mobie_northstar.data.model

import com.google.gson.annotations.SerializedName

/**
 * GeoJSON FeatureCollection cho các điểm trạm quan trắc ngập lụt
 */
data class FloodFeatureCollection(
    @SerializedName("type") val type: String? = null,
    @SerializedName("features") val features: List<FloodFeature> = emptyList()
)

data class FloodFeature(
    @SerializedName("type") val type: String? = null,
    @SerializedName("geometry") val geometry: FloodGeometry? = null,
    @SerializedName("properties") val properties: FloodProperties? = null
)

data class FloodGeometry(
    @SerializedName("type") val type: String? = null,
    @SerializedName("coordinates") val coordinates: List<Double> = emptyList()
) {
    val lng: Double get() = coordinates.getOrNull(0) ?: 105.80
    val lat: Double get() = coordinates.getOrNull(1) ?: 21.02
}

data class FloodProperties(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "Trạm quan trắc ngập",
    @SerializedName("sensor_id") val sensorId: String? = null,
    @SerializedName("threshold_cm") val thresholdCm: Double = 20.0,
    @SerializedName("depth_cm") val depthCm: Double = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("last_reading_at") val lastReadingAt: String? = null,
    @SerializedName("water_trend") val waterTrend: String? = null,
    @SerializedName("estimated_drainage_time") val estimatedDrainageTime: String? = null
) {
    val isDanger: Boolean get() = depthCm >= thresholdCm || depthCm >= 20.0
    val isWarning: Boolean get() = depthCm >= 5.0 && depthCm < 20.0
}
