import cv2
import os
import logging
from ultralytics import YOLO
from paddleocr import PaddleOCR

# Tắt log rác
logging.getLogger("ppocr").setLevel(logging.WARNING)


class OCRProcessor:
    def __init__(self, model_path: str):
        self.model_path = model_path
        if os.path.exists(self.model_path):
            print(f"Đang tải model từ {self.model_path} ...")
            self.detector = YOLO(self.model_path)
            self.ocr = PaddleOCR(lang='en', use_angle_cls=True, show_log=False)
            print("Đã tải model AI thành công!")
        else:
            raise FileNotFoundError(f"Không tìm thấy model tại {self.model_path}")

    def process_image(self, image_path: str):
        img = cv2.imread(image_path)
        if img is None:
            print("Lỗi: Không đọc được ảnh.")
            return []

        results = self.detector(img)
        found_plates = []

        for result in results:
            boxes = result.boxes
            for box in boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                conf = box.conf[0]

                if conf < 0.4: continue

                # 1. Cắt ảnh biển số
                plate_img = img[y1:y2, x1:x2]

                try:
                    # BƯỚC QUAN TRỌNG: Bật det=True để tìm các dòng chữ riêng biệt
                    result_ocr = self.ocr.ocr(plate_img, det=True, cls=False)

                    if result_ocr and result_ocr[0]:
                        lines = result_ocr[0]
                        # Sắp xếp theo trục Y từ trên xuống dưới
                        if len(lines) > 1:
                            lines = sorted(lines, key=lambda r: r[0][0][1])

                        final_text_parts = []
                        for line in lines:
                            text = line[1][0]
                            score = line[1][1]

                            if score > 0.6:
                                clean_text = text.replace('.', '').replace('-', '').replace(' ', '')
                                final_text_parts.append(clean_text)

                        if final_text_parts:
                            full_text = "".join(final_text_parts)
                            found_plates.append(full_text)
                    else:
                        # FALLBACK: Nếu det=True không tìm thấy
                        rec_result = self.ocr.ocr(plate_img, det=False, cls=False)
                        if rec_result and isinstance(rec_result[0], tuple):
                            text = rec_result[0][0]
                            found_plates.append("".join(c for c in text if c.isalnum()))

                except Exception as e:
                    print(f"Lỗi xử lý OCR: {e}")

        return found_plates