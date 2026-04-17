import os
import shutil
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
import uvicorn

from app.core.config import settings
from app.service.ocr_processor import OCRProcessor

app = FastAPI(title=settings.PROJECT_NAME)

# Khởi tạo processor ngay khi server chạy
try:
    processor = OCRProcessor(settings.MODEL_PATH)
except Exception as e:
    print(f"Lỗi khởi tạo AI Models: {e}")
    processor = None


@app.get("/health")
async def health_check():
    """Health check endpoint cho Docker healthcheck"""
    if processor is None:
        raise HTTPException(status_code=503, detail="AI Model chưa được tải")
    return {
        "status": "healthy",
        "model_loaded": True
    }


@app.post("/api/v1/ai/detect-plate")
async def detect_plate_api(file: UploadFile = File(...)):
    if processor is None:
        raise HTTPException(status_code=500, detail="AI Model chưa được tải")

    temp_file_path = f"temp_{file.filename}"

    # Lưu file ảnh tải lên từ Spring Boot vào ổ cứng tạm
    with open(temp_file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    try:
        # Gọi hàm xử lý AI
        plates = processor.process_image(temp_file_path)

        final_plate = plates[0] if len(plates) > 0 else None

        return JSONResponse(content={
            "success": True if final_plate else False,
            "plateNumber": final_plate["plate"] if final_plate else None,
            "confidence": final_plate["confidence"] if final_plate else None,
            "message": "OCR xử lý thành công" if final_plate else "Không đọc được biển số"
        })
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý ảnh: {str(e)}")
    finally:
        # Luôn dọn dẹp file tạm để tránh rác ổ cứng
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)