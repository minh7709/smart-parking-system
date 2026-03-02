# 🚀 Hướng dẫn Cài đặt & Quy trình Làm việc Nhóm (Smart Parking)

Tài liệu này hướng dẫn các thành viên trong team cách thiết lập môi trường, cài đặt thư viện và quy trình kéo code của nhau về chạy thử nghiệm trên máy cá nhân.

## 🛠️ 1. Yêu cầu Hệ thống (Mọi người đều phải cài)
Dù bạn làm phân hệ nào, hãy đảm bảo máy tính đã cài đặt sẵn:
* **Git:** Quản lý mã nguồn.
* **Docker Desktop:** Để chạy Database PostgreSQL.
* **Java 17 (JDK 17):** Môi trường chạy Spring Boot.
* **Node.js (v18+):** Môi trường chạy React / Electron.
* **Python (3.10+):** Môi trường chạy AI OCR.
* **IntelliJ IDEA (Community/Ultimate):** IDE chuẩn để mở và chạy code Backend.

---

## 🔄 2. Quy trình Git cho Team 3 Người (Feature Branch Flow)

Để tránh code đè lên nhau (Conflict), team sẽ làm việc theo quy tắc sau:
* Nhánh `main`: Nơi chứa code hoàn chỉnh, chạy ổn định (Tuyệt đối không push code đang lỗi lên đây).
* Nhánh `feature/backend`: Dành riêng cho dev Java.
* Nhánh `feature/frontend`: Dành riêng cho dev React.
* Nhánh `feature/ai`: Dành riêng cho dev Python.

**Quy trình làm việc hàng ngày:**
1. Mở terminal, chuyển về nhánh của mình: `git checkout <tên_nhánh>`
2. Lấy code mới nhất về trước khi code: `git pull origin <tên_nhánh>`
3. Code tính năng mới...
4. Lưu code: `git add .` -> `git commit -m "feat: cập nhật..."`
5. Đẩy lên Git: `git push origin <tên_nhánh>`

---

## ☕ 3. Dành cho Dev Frontend & AI: Cách kéo và chạy Backend

Dev Frontend và AI cần API để test. Làm theo 3 bước sau để tự bật Server Backend ở localhost:8080.

### Bước 3.1: Chạy Database (PostgreSQL)
Mọi API đều cần chọc vào Database.
1. Mở Terminal (Bash/CMD) tại thư mục gốc của project.
2. Khởi chạy Docker: 
   ```bash
   docker-compose down -v  # Xóa data cũ (nếu có)
   docker-compose up -d    # Chạy db ngầm