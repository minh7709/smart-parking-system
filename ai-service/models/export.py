from ultralytics import YOLO

# Load model gốc của bạn (ví dụ: best.pt)
model = YOLO("license_plate_detector.pt")

# Xuất ra định dạng ONNX
model.export(format="onnx") 
# Kết quả: Bạn sẽ thu được file 'license_plate_detector.onnx'. Copy file này vào thư mục dự án Docker.