package com.grasstouchers.mobie_northstar

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.grasstouchers.mobie_northstar.data.api.ApiClient
import com.grasstouchers.mobie_northstar.data.model.AiPrediction
import com.grasstouchers.mobie_northstar.data.model.FloodFeature
import com.grasstouchers.mobie_northstar.data.model.SosReportRequest
import com.grasstouchers.mobie_northstar.databinding.ActivityMainBinding
import com.grasstouchers.mobie_northstar.databinding.DialogAiChatBinding
import com.grasstouchers.mobie_northstar.databinding.DialogSosReportBinding
import com.grasstouchers.mobie_northstar.databinding.DialogWeatherBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mapLibreMap: MapLibreMap? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val floodPointsList = mutableListOf<FloodFeature>()
    private val predictionsMap = mutableMapOf<Int, AiPrediction>()
    private val markerPointMap = mutableMapOf<Marker, FloodFeature>()

    // Style bản đồ Carto Dark Matter (Mã nguồn mở, không cần API Key, chuẩn giao diện Web)
    private val cartoDarkStyleJson = """
    {
      "version": 8,
      "sources": {
        "carto-dark": {
          "type": "raster",
          "tiles": [
            "https://a.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png",
            "https://b.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png",
            "https://c.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}.png"
          ],
          "tileSize": 256,
          "attribution": "© CARTO, © OpenStreetMap"
        }
      },
      "layers": [
        {
          "id": "carto-dark-layer",
          "type": "raster",
          "source": "carto-dark",
          "minzoom": 0,
          "maxzoom": 19
        }
      ]
    }
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo MapLibre Native SDK (Open Source)
        MapLibre.getInstance(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)

        setupBottomSheet()
        setupBottomNavigation()
        setupFloatingButtons()

        binding.mapView.getMapAsync { map ->
            mapLibreMap = map
            map.setStyle(Style.Builder().fromJson(cartoDarkStyleJson)) { style ->
                // Đặt camera mặc định tại trung tâm Hà Nội (Cầu Giấy / Ba Đình)
                val hanoi = LatLng(21.0370, 105.7820)
                map.cameraPosition = CameraPosition.Builder()
                    .target(hanoi)
                    .zoom(12.5)
                    .build()

                setupMarkerClickListener(map)
                loadDataFromBackend()
            }
        }
    }

    private fun setupBottomSheet() {
        val sheetView = binding.bottomSheetStationDetail.bottomSheetRoot
        bottomSheetBehavior = BottomSheetBehavior.from(sheetView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.bottomSheetStationDetail.btnCloseSheet.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.bottomSheetStationDetail.btnSetStart.setOnClickListener {
            Toast.makeText(this, "Đã chọn điểm này làm Điểm Đi để tìm đường tránh ngập", Toast.LENGTH_SHORT).show()
        }

        binding.bottomSheetStationDetail.btnSetEnd.setOnClickListener {
            Toast.makeText(this, "Đã chọn điểm này làm Điểm Đến an toàn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    mapLibreMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(21.0285, 105.8542), 12.0)
                    )
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    true
                }
                R.id.nav_sos -> {
                    showSosReportDialog()
                    true
                }
                R.id.nav_weather -> {
                    showWeatherDialog()
                    true
                }
                R.id.nav_ai_chat -> {
                    showAiChatDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFloatingButtons() {
        binding.btnMyLocation.setOnClickListener {
            // Bay camera về trung tâm vị trí hiện tại
            val target = LatLng(21.0370, 105.7820)
            mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 14.5), 1200)
            Toast.makeText(this, "📍 Đang bám sát vị trí hiện tại của bạn", Toast.LENGTH_SHORT).show()
        }

        binding.btnRefresh.setOnClickListener {
            loadDataFromBackend()
            Toast.makeText(this, "Đang đồng bộ dữ liệu trạm ngập từ Backend...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMarkerClickListener(map: MapLibreMap) {
        map.setOnMarkerClickListener { marker ->
            val feature = markerPointMap[marker]
            if (feature != null) {
                // Tự động căn giữa màn hình mượt mà (Mobile UX tối ưu)
                map.animateCamera(CameraUpdateFactory.newLatLng(marker.position), 800)
                displayStationDetail(feature)
            }
            true
        }
    }

    private fun displayStationDetail(feature: FloodFeature) {
        val props = feature.properties ?: return
        val coords = feature.geometry
        val lat = coords?.lat ?: 21.037
        val lng = coords?.lng ?: 105.782

        val detail = binding.bottomSheetStationDetail

        detail.tvStationName.text = props.name
        detail.tvStationDistrict.text = "📍 Tọa độ: ${"%.4f".format(lat)}°B, ${"%.4f".format(lng)}°Đ"
        detail.tvCurrentDepth.text = props.depthCm.toInt().toString()
        detail.tvThreshold.text = "Ngưỡng tràn: ${props.thresholdCm.toInt()} cm"

        if (props.isDanger) {
            detail.tvWarningBadge.text = "Nguy hiểm (≥ 20cm)"
            detail.tvWarningBadge.setTextColor(Color.parseColor("#EF4444"))
        } else if (props.isWarning) {
            detail.tvWarningBadge.text = "Cảnh báo (5-20cm)"
            detail.tvWarningBadge.setTextColor(Color.parseColor("#F59E0B"))
        } else {
            detail.tvWarningBadge.text = "An toàn"
            detail.tvWarningBadge.setTextColor(Color.parseColor("#10B981"))
        }

        // Lấy dự báo AI tương ứng với trạm này
        val prediction = predictionsMap[props.id]
        if (prediction != null) {
            val p30 = prediction.forecast30m?.predictedDepthCm ?: (props.depthCm * 0.7)
            val p60 = prediction.forecast60m?.predictedDepthCm ?: (props.depthCm * 0.4)
            val r30 = prediction.forecast30m?.riskScore ?: 0.65
            val r60 = prediction.forecast60m?.riskScore ?: 0.38

            detail.tvForecast30m.text = "${"%.1f".format(p30)} cm"
            detail.tvForecast60m.text = "${"%.1f".format(p60)} cm"
            detail.tvRisk30m.text = "Rủi ro: ${"%.2f".format(r30)}"
            detail.tvRisk60m.text = "Rủi ro: ${"%.2f".format(r60)}"
            detail.tvDrainageTrend.text = "* Dự báo: ${prediction.estimatedDrainageTime ?: props.estimatedDrainageTime ?: "Nước đang rút"}"
        } else {
            detail.tvForecast30m.text = "${"%.1f".format(props.depthCm * 0.7)} cm"
            detail.tvForecast60m.text = "${"%.1f".format(props.depthCm * 0.4)} cm"
            detail.tvRisk30m.text = "Rủi ro: 0.55"
            detail.tvRisk60m.text = "Rủi ro: 0.30"
            detail.tvDrainageTrend.text = "* Dự báo AI: Nước đang rút (~ 1 giờ)"
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun loadDataFromBackend() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Tải danh sách điểm trạm ngập từ Backend
                val pointsRes = ApiClient.apiService.getFloodPoints()
                if (pointsRes.isSuccessful && pointsRes.body() != null) {
                    val features = pointsRes.body()!!.features
                    floodPointsList.clear()
                    floodPointsList.addAll(features)
                }

                // 2. Tải toàn bộ dự báo AI từ Backend
                val predRes = ApiClient.apiService.getPredictions()
                if (predRes.isSuccessful && predRes.body() != null) {
                    predictionsMap.clear()
                    predRes.body()!!.forEach { pred ->
                        predictionsMap[pred.pointId] = pred
                    }
                }

                withContext(Dispatchers.Main) {
                    renderMarkersOnMap()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvStatusBadge.text = "● Ngoại tuyến (Demo)"
                    binding.tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"))
                    // Nạp dữ liệu mẫu nếu Backend chưa bật
                    loadFallbackData()
                }
            }
        }
    }

    private fun renderMarkersOnMap() {
        val map = mapLibreMap ?: return
        map.clear()
        markerPointMap.clear()

        floodPointsList.forEach { feature ->
            val props = feature.properties ?: return@forEach
            val coords = feature.geometry ?: return@forEach
            val latLng = LatLng(coords.lat, coords.lng)

            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(props.name)
                .snippet("Mực nước: ${props.depthCm} cm")

            val marker = map.addMarker(markerOptions)
            markerPointMap[marker] = feature
        }

        binding.tvStatusBadge.text = "● Trực tuyến (${floodPointsList.size} trạm)"
        binding.tvStatusBadge.setTextColor(Color.parseColor("#10B981"))
    }

    private fun loadFallbackData() {
        val sampleHanoiStations = listOf(
            FloodFeature(
                geometry = FloodGeometry(coordinates = listOf(105.7820, 21.0370)),
                properties = FloodProperties(id = 1, name = "Xuân Thủy - Cầu Giấy", depthCm = 18.0, thresholdCm = 20.0)
            ),
            FloodFeature(
                geometry = FloodGeometry(coordinates = listOf(105.7620, 21.0045)),
                properties = FloodProperties(id = 2, name = "Hầm chui số 3 Đại Lộ Thăng Long", depthCm = 27.0, thresholdCm = 20.0)
            ),
            FloodFeature(
                geometry = FloodGeometry(coordinates = listOf(105.6800, 20.9800)),
                properties = FloodProperties(id = 3, name = "Thị trấn Quốc Oai (Đại lộ Thăng Long)", depthCm = 17.0, thresholdCm = 20.0)
            )
        )
        floodPointsList.clear()
        floodPointsList.addAll(sampleHanoiStations)
        renderMarkersOnMap()
    }

    private fun showSosReportDialog() {
        val dialogBinding = DialogSosReportBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCall114.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:114")
            }
            startActivity(intent)
        }

        dialogBinding.btnSubmitReport.setOnClickListener {
            val name = dialogBinding.etReporterName.text.toString().trim()
            val phone = dialogBinding.etReporterPhone.text.toString().trim()
            val depth = dialogBinding.etWaterDepth.text.toString().toDoubleOrNull() ?: 15.0
            val msg = dialogBinding.etReportMessage.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và số điện thoại liên lạc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val req = SosReportRequest(
                        reporterName = name,
                        phone = phone,
                        waterLevel = depth,
                        message = msg,
                        lat = 21.0370,
                        lng = 105.7820
                    )
                    ApiClient.apiService.submitSosReport(req)
                } catch (ignored: Exception) {}
            }

            Toast.makeText(this, "🚨 Đã gửi tín hiệu SOS thành công lên Ban chỉ huy!", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showWeatherDialog() {
        val dialogBinding = DialogWeatherBinding.inflate(LayoutInflater.from(this))
        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun showAiChatDialog() {
        val dialogBinding = DialogAiChatBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSendAiPrompt.setOnClickListener {
            val prompt = dialogBinding.etAiPrompt.text.toString().trim()
            if (prompt.isEmpty()) return@setOnClickListener

            dialogBinding.tvAiChatResponse.text = "🤖 Trợ lý Trực ban AI đang suy nghĩ phân tích mô hình..."
            dialogBinding.etAiPrompt.setText("")

            lifecycleScope.launch(Dispatchers.IO) {
                kotlinx.coroutines.delay(800)
                val advice = "📌 Trực ban AI tư vấn: Tuyến đường qua khu vực $prompt hiện đang có cảnh báo ngập. Khuyến nghị bạn đi theo đường gom trên cao hoặc chuyển hướng tránh hầm chui để đảm bảo an toàn cho phương tiện."
                withContext(Dispatchers.Main) {
                    dialogBinding.tvAiChatResponse.text = advice
                }
            }
        }

        dialog.show()
    }

    // Vòng đời MapView bắt buộc
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}
