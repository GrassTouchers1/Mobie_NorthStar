package com.grasstouchers.mobie_northstar.data.model

import com.google.gson.annotations.SerializedName

/**
 * Báo cáo ngập lụt khẩn cấp & Tín hiệu SOS gửi lên Ban chỉ huy
 */
data class SosReportRequest(
    @SerializedName("reporter_name") val reporterName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("water_level") val waterLevel: Double?,
    @SerializedName("message") val message: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)
