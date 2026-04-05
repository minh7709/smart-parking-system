import os

class Settings:
    PROJECT_NAME: str = "Smart Parking AI Service"
    BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    MODEL_PATH = os.path.join(BASE_DIR, "models", "license_plate_detector.onnx")

settings = Settings()