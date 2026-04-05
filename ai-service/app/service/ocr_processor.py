import cv2
import numpy as np
import onnxruntime as ort
from rapidocr_onnxruntime import RapidOCR
from PIL import Image, ImageOps


class OCRProcessor:
    def __init__(self, det_model_path: str):
        print(f"Đang tải YOLO ONNX model từ {det_model_path} ...")
        self.det_session = ort.InferenceSession(det_model_path)

        print("Đang tải RapidOCR model ...")
        self.ocr = RapidOCR()
        print("Đã tải toàn bộ model AI thành công!")

    def fix_exif_orientation(self, image_path):
        """Đọc ảnh và tự động xoay đúng chiều thực tế dựa trên EXIF"""
        try:
            img = Image.open(image_path)
            img = ImageOps.exif_transpose(img)
            img_cv = cv2.cvtColor(np.array(img), cv2.COLOR_RGB2BGR)
            return img_cv
        except Exception as e:
            print(f"Lỗi đọc ảnh: {e}")
            return None

    def preprocess_yolo(self, img, size=(640, 640)):
        """Chuẩn bị ảnh đầu vào cho YOLOv8"""
        h, w = img.shape[:2]
        img_resized = cv2.resize(img, size)
        # Model thường được train với ảnh RGB
        img_rgb = cv2.cvtColor(img_resized, cv2.COLOR_BGR2RGB)
        img_data = img_rgb.transpose(2, 0, 1).astype(np.float32)
        img_data /= 255.0
        return np.expand_dims(img_data, axis=0), w, h

    def nms(self, boxes, scores, iou_threshold=0.45):
        """Non-Maximum Suppression"""
        indices = cv2.dnn.NMSBoxes(boxes, scores, 0.25, iou_threshold)
        return indices

    def process_image(self, image_path: str):
        img = self.fix_exif_orientation(image_path)
        if img is None:
            return []

        orig_h, orig_w = img.shape[:2]

        # --- BƯỚC 1: TÌM BIỂN SỐ BẰNG YOLO ONNX ---
        input_data, _, _ = self.preprocess_yolo(img)
        outputs = self.det_session.run(None, {self.det_session.get_inputs()[0].name: input_data})

        output = np.squeeze(outputs[0])
        boxes, scores = [], []

        rows = output.T
        for row in rows:
            prob = row[4]
            if prob > 0.4:
                xc, yc, w, h = row[:4]
                x1 = (xc - w / 2) * (orig_w / 640)
                y1 = (yc - h / 2) * (orig_h / 640)
                boxes.append([int(x1), int(y1), int(w * orig_w / 640), int(h * orig_h / 640)])
                scores.append(float(prob))

        result_indices = self.nms(boxes, scores)
        found_plates = []
        # --- BƯỚC 2: CẮT ẢNH VÀ ĐỌC CHỮ BẰNG RAPIDOCR ---
        for i in result_indices:
            x, y, w, h = boxes[i]

            # Tăng padding lên một chút để bao trọn các biển số dài (như 29A-000.03)
            pad_x = int(w * 0.04)
            pad_y = int(h * 0.05)

            x_start, y_start = max(0, x - pad_x), max(0, y - pad_y)
            x_end, y_end = min(orig_w, x + w + pad_x), min(orig_h, y + h + pad_y)

            plate_img = img[y_start:y_end, x_start:x_end]

            if plate_img.shape[0] < 10 or plate_img.shape[1] < 10:
                continue

            try:
                # SỬA LỖI 1: Tên tham số đúng để tắt tính năng lật chữ là `use_cls=False`
                ocr_result, _ = self.ocr(plate_img, use_det=True, use_cls=False, use_rec=True)

                if ocr_result:
                    # GOM DÒNG (LINE GROUPING)
                    ocr_result.sort(key=lambda r: r[0][0][1])

                    lines = []
                    current_line = []

                    for r in ocr_result:
                        if not current_line:
                            current_line.append(r)
                        else:
                            prev_y_center = (current_line[-1][0][0][1] + current_line[-1][0][3][1]) / 2
                            curr_y_center = (r[0][0][1] + r[0][3][1]) / 2
                            box_h = r[0][3][1] - r[0][0][1]

                            # Nới lỏng dung sai gom dòng lên 0.6 để tránh tách nhầm dòng
                            if abs(curr_y_center - prev_y_center) < box_h * 0.6:
                                current_line.append(r)
                            else:
                                lines.append(current_line)
                                current_line = [r]

                    if current_line:
                        lines.append(current_line)

                    plate_text = ""
                    for line in lines:
                        line.sort(key=lambda r: r[0][0][0])
                        for r in line:
                            text = r[1]
                            conf = r[2]

                            # SỬA LỖI 2: Hạ ngưỡng tin cậy xuống 0.2 để không bị mất cụm số có chứa dấu chấm/gạch
                            if conf > 0.3:
                                plate_text += text

                    # Làm sạch: Chỉ giữ lại chữ cái và số
                    clean_text = "".join(c for c in plate_text if c.isalnum())
                    if clean_text:
                        found_plates.append(clean_text)
            except Exception as e:
                print(f"Lỗi xử lý RapidOCR: {e}")

        return found_plates