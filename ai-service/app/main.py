import os
from pydantic import BaseModel
from fastapi import FastAPI, HTTPException
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


class DetectPlateRequest(BaseModel):
    imagePath: str | None = None
    imageUrl: str | None = None


def load_image_from_path(request: DetectPlateRequest):
    image_path = request.imagePath or request.imageUrl
    if image_path is None or not image_path.strip():
        raise HTTPException(status_code=400, detail="Thiếu imagePath hoặc imageUrl")

    normalized_path = os.path.abspath(image_path.strip())
    if not os.path.exists(normalized_path):
        raise HTTPException(status_code=404, detail=f"Không tìm thấy ảnh tại đường dẫn: {normalized_path}")
    if not os.path.isfile(normalized_path):
        raise HTTPException(status_code=400, detail=f"Đường dẫn không phải file ảnh: {normalized_path}")

    img = processor.load_image_from_path(normalized_path)
    if img is None:
        raise HTTPException(status_code=400, detail="Không thể đọc ảnh từ đường dẫn đã cung cấp")

    return normalized_path, img


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
async def detect_plate_api(request: DetectPlateRequest):
    if processor is None:
        raise HTTPException(status_code=500, detail="AI Model chưa được tải")

    try:
        _, img = load_image_from_path(request)
        plates = processor.process_loaded_image(img)

        final_plate = plates[0] if len(plates) > 0 else None

        return JSONResponse(content={
            "success": True if final_plate else False,
            "plateNumber": final_plate["plate"] if final_plate else None,
            "confidence": final_plate["confidence"] if final_plate else None,
            "message": "OCR xử lý thành công" if final_plate else "Không đọc được biển số"
        })
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi xử lý ảnh: {str(e)}")


if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8000)