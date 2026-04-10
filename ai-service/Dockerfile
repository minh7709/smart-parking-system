# 1. Sử dụng Python 3.10 slim là lựa chọn rất tốt cho môi trường Production
FROM python:3.10-slim

# 2. Thiết lập các biến môi trường quan trọng
# PYTHONUNBUFFERED: Giúp log hiện ra ngay lập tức trên terminal (không bị nghẽn)
# PYTHONDONTWRITEBYTECODE: Không tạo file .pyc rác trong container
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    PIP_NO_CACHE_DIR=1

# 3. Cài đặt thư viện hệ thống (Đã fix lỗi libgl1)
RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 4. Thiết lập thư mục làm việc
WORKDIR /app
COPY requirements.txt .
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# 6. Copy toàn bộ source code
COPY . .

# 8. Mở port 8000
EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]