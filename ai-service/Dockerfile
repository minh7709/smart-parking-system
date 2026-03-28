# Sử dụng Python 3.12 slim cho nhẹ gọn
FROM python:3.12-slim

# Cài đặt các thư viện hệ thống cần thiết cho OpenCV và Paddle
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    && rm -rf /var/lib/apt/lists/*

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy requirements và cài đặt
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy toàn bộ source code (bao gồm thư mục models, app) vào container
COPY . .

# Mở port 8000
EXPOSE 8000

# Lệnh khởi chạy FastAPI server
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]