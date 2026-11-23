# ⭐ NORTHSTAR Mobile (Mobie_NorthStar)
> **Ứng dụng di động cảnh báo ngập lụt, cứu hộ thiên tai và chỉ đường an toàn cho người dân.**  
> Dự án tham gia **Hội thi Olympic Tin học Sinh viên Việt Nam & Phần mềm Nguồn mở (OLP 2026)**.

---

## 📱 Giới thiệu tổng quan
**NORTHSTAR Mobile** là phiên bản ứng dụng di động Native (Android) được phát triển riêng cho người dân tham gia giao thông trong mùa mưa lũ. Ứng dụng kết nối trực tiếp với hệ sinh thái **NORTHSTAR Core & Backend**, cung cấp thông tin cảnh báo ngập theo thời gian thực và mô hình **Dự báo ngập bằng Trí tuệ Nhân tạo (AI)** sau 30 phút và 60 phút.

---

## 🌟 Tính năng nổi bật trên Mobile

1. **Bản đồ số ngập lụt tương tác cao (MapLibre Open-Source)**:
   - Hiển thị trực quan toàn bộ các điểm trạm quan trắc ngập lụt tại Hà Nội.
   - Phân cấp rủi ro rõ ràng theo mã màu:
     - 🔴 **Nguy hiểm**: Mực nước $\ge 20\text{ cm}$ (vượt ngưỡng an toàn cho xe máy, ô tô con).
     - 🟡 **Cảnh báo**: Mực nước $5 - 20\text{ cm}$.
     - 🟢 **An toàn**: Mực nước $< 5\text{ cm}$.
2. **Card trượt đáy thông minh (Bottom Sheet Station Detail)**:
   - Khi bấm vào bất kỳ điểm ngập nào, bản đồ tự động căn giữa màn hình mượt mà.
   - Hiển thị đầy đủ thông số: Mực nước đo được, ngưỡng tràn, trạng thái cảm biến IoT.
   - **Tích hợp Dự báo ngập bằng AI**: Hiển thị dự báo mực nước và chỉ số rủi ro sau **30 phút** và **60 phút**, xu thế nước rút và thời gian ước tính tiêu thoát.
   - Hỗ trợ chọn nhanh **Điểm Đi** / **Điểm Đến** để tìm lộ trình tránh ngập.
3. **Báo cáo ngập lụt cộng đồng & Tín hiệu SOS**:
   - Cho phép người dân gửi tọa độ GPS hiện trường, mô tả tình trạng ngập và gọi khẩn cấp **114 (Cứu nạn Cứu hộ)** chỉ bằng 1 chạm.
4. **Bản tin Khí tượng & Cảnh báo thiên tai**:
   - Tích hợp dữ liệu thời tiết thời gian thực từ Open-Meteo và Trung tâm Dự báo Khí tượng Thủy văn Quốc gia (NCHMF).
5. **Trợ lý Trực ban Cứu hộ AI**:
   - Khung tương tác AI hỗ trợ hỏi đáp kỹ năng thoát hiểm, an toàn di chuyển và cứu trợ khẩn cấp mùa lũ 24/7.

---

## 🛠️ Công nghệ & Thư viện Mã Nguồn Mở (100% Open Source)
Ứng dụng tuân thủ nghiêm ngặt tiêu chí bản quyền phần mềm nguồn mở của Hội thi OLP:
- **Ngôn ngữ**: Kotlin (Apache-2.0).
- **Map Engine**: **MapLibre Native Android SDK** (`org.maplibre.gl:android-sdk:11.5.2`) - Bản đồ vector/raster mã nguồn mở chuẩn quốc tế (BSD-2 License), thay thế hoàn toàn các nền tảng thương mại độc quyền.
- **Bản đồ nền (Base Tiles)**: Carto Dark Matter / OpenStreetMap (ODbL / CC-BY).
- **Giao diện di động**: Material Components 3 & AndroidX (Apache-2.0).
- **Giao thức mạng**: Square Retrofit 2 & OkHttp 3 (Apache-2.0).
- **Xử lý bất đồng bộ**: Kotlin Coroutines & Android Lifecycle ViewModel (Apache-2.0).

---

## 🚀 Hướng dẫn mở và chạy trên Android Studio

### 1. Mở dự án trong Android Studio
1. Khởi động **Android Studio**.
2. Chọn **File** -> **Open...**
3. Tìm và chọn thư mục: `D:\OpenSource_2026\Mobie_NorthStar`
4. Chờ Android Studio đồng bộ Gradle (**Gradle Sync**).

### 2. Chạy trên Máy ảo (Android Emulator)
1. Trong Android Studio, mở menu **Device Manager** và khởi động một Máy ảo Android bất kỳ (khuyến nghị Android 10, 11, 12, 13 hoặc 14).
2. Kiểm tra Backend FastAPI đang chạy tại máy tính: `http://127.0.0.1:8000`.
   *(Ứng dụng đã được cấu hình tự động trỏ tới `http://10.0.2.2:8000/api` để máy ảo kết nối thẳng vào Backend của máy tính).*
3. Nhấn nút **Run (▶)** (hoặc phím tắt `Shift + F10`) để cài đặt và trải nghiệm ứng dụng trên máy ảo!

---

## 👥 Nhóm tác giả
- **Tổ chức**: `GrassTouchers1`
- **Dự án**: NORTHSTAR Ecosystem
- **Giấy phép**: Apache License 2.0
