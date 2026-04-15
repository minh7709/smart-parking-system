import React, { useRef, useState, useEffect } from "react";
import { Card, Button, Space, Tooltip, message, Spin } from "antd";
import {
  CameraOutlined,
  ZoomInOutlined,
  SettingOutlined,
  ExpandOutlined,
} from "@ant-design/icons";
import axiosClient from "./../../../api/axiosClient";

const CameraCard = ({ title, type, laneId, videoSrc, onSuccess }) => {
  const imgRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [detectedPlate, setDetectedPlate] = useState(null);
  const [imgError, setImgError] = useState(false);
  const themeColor = "#ffffff";

  // Hàm chụp ảnh từ thẻ img (frame hiện tại)
  const handleCaptureAndSend = async () => {
    const img = imgRef.current;
    if (!img || !img.complete || img.naturalWidth === 0) {
      message.error("Hình ảnh chưa sẵn sàng");
      return;
    }

    // Tạo canvas từ img
    const canvas = document.createElement("canvas");
    canvas.width = img.naturalWidth;
    canvas.height = img.naturalHeight;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

    const blob = await new Promise((resolve) =>
      canvas.toBlob(resolve, "image/jpeg", 0.9),
    );
    if (!blob) {
      message.error("Không thể chụp ảnh");
      return;
    }
    const file = new File([blob], `capture_${Date.now()}.jpg`, {
      type: "image/jpeg",
    });

    setLoading(true);
    try {
      const formData = new FormData();
      const requestPayload = { laneId: laneId };
      formData.append(
        "request",
        new Blob([JSON.stringify(requestPayload)], {
          type: "application/json",
        }),
      );
      formData.append("image", file);

      const endpoint =
        type === "IN"
          ? "/v1/guard/parking-session/check-in"
          : "/v1/guard/parking-session/check-out";

      const response = await axiosClient.postForm(endpoint, formData);

      if (response?.success) {
        const plate = response?.data?.plateNumber || "Đã nhận diện";
        setDetectedPlate(plate);
        message.success(
          `${type === "IN" ? "Check-in" : "Check-out"} thành công: ${plate}`,
        );
        if (onSuccess) onSuccess(response.data);
      } else {
        message.error(response?.message || "Lỗi từ server");
      }
    } catch (error) {
      console.error(error);
      message.error(error.message || "Gửi ảnh thất bại");
    } finally {
      setLoading(false);
    }
  };

  // Nếu có lỗi CORS khi dùng canvas với ảnh từ nguồn ngoài,
  // bạn cần cấu hình proxy trong vite.config.js (hướng dẫn bên dưới)
  // Hiện tại code vẫn hoạt động nếu DroidCam cho phép CORS hoặc dùng proxy.

  return (
    <Card
      title={
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <span
            style={{
              width: 8,
              height: 8,
              borderRadius: "50%",
              background: themeColor,
              boxShadow: `0 0 8px ${themeColor}`,
            }}
          />
          <span style={{ color: "#fff", fontWeight: 600, letterSpacing: 1 }}>
            {title}
          </span>
        </div>
      }
      extra={
        <Space size="small">
          <Tooltip title="Chụp & gửi">
            <Button
              type="primary"
              shape="circle"
              icon={<CameraOutlined />}
              onClick={handleCaptureAndSend}
              loading={loading}
              style={{ backgroundColor: "#00b96b" }}
            />
          </Tooltip>
          <Tooltip title="Phóng to">
            <Button
              type="text"
              shape="circle"
              icon={<ZoomInOutlined style={{ color: "#fff" }} />}
            />
          </Tooltip>
          <Tooltip title="Cài đặt luồng">
            <Button
              type="text"
              shape="circle"
              icon={<SettingOutlined style={{ color: "#fff" }} />}
            />
          </Tooltip>
          <Tooltip title="Mở toàn màn hình">
            <Button
              type="text"
              shape="circle"
              icon={<ExpandOutlined style={{ color: "#fff" }} />}
            />
          </Tooltip>
        </Space>
      }
      variant="borderless"
      style={{
        background: "#141414",
        border: "1px solid #1f1f1f",
        borderRadius: 16,
        overflow: "hidden",
      }}
      styles={{
        header: { borderBottom: "1px solid #1f1f1f", padding: "12px 16px" },
        body: { padding: 12 },
      }}
    >
      <div
        style={{
          height: 260,
          borderRadius: 8,
          overflow: "hidden",
          position: "relative",
          background: "#000",
        }}
      >
        {imgError ? (
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              height: "100%",
              color: "#ff4d4f",
              background: "#1a1a1a",
              textAlign: "center",
              padding: 20,
            }}
          >
            <span>
              Không thể kết nối camera.
              <br />
              Kiểm tra IP DroidCam
            </span>
          </div>
        ) : (
          <img
            ref={imgRef}
            src={videoSrc}
            alt="camera-feed"
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
            onError={() => {
              setImgError(true);
              message.error("Lỗi kết nối camera. Kiểm tra IP DroidCam.");
            }}
            onLoad={() => setImgError(false)}
            // Nếu gặp lỗi CORS khi dùng canvas, hãy bỏ dòng dưới hoặc cấu hình proxy
            crossOrigin="anonymous"
          />
        )}
        <div
          style={{
            position: "absolute",
            inset: 0,
            background:
              "linear-gradient(to top, rgba(0,0,0,0.9) 0%, transparent 40%)",
            pointerEvents: "none",
          }}
        />
        <div
          style={{
            position: "absolute",
            top: 12,
            left: 12,
            background: "rgba(0,0,0,0.6)",
            backdropFilter: "blur(4px)",
            padding: "4px 10px",
            borderRadius: 4,
          }}
        >
          <span style={{ color: "#fff", fontSize: 10, fontWeight: 700 }}>
            LIVE
          </span>
        </div>
        {detectedPlate && (
          <div
            style={{
              position: "absolute",
              bottom: 5,
              left: "50%",
              transform: "translateX(-50%)",
              background: "rgba(0,0,0,0.7)",
              backdropFilter: "blur(8px)",
              padding: "6px 32px",
              borderRadius: 8,
              border: `1px solid ${themeColor}`,
            }}
          >
            <span
              style={{
                color: themeColor,
                fontSize: 15,
                fontWeight: 900,
                letterSpacing: 2,
                fontFamily: "monospace",
              }}
            >
              {detectedPlate}
            </span>
          </div>
        )}
        {loading && (
          <div
            style={{
              position: "absolute",
              top: "50%",
              left: "50%",
              transform: "translate(-50%, -50%)",
            }}
          >
            <Spin size="large" />
          </div>
        )}
      </div>
    </Card>
  );
};

export default CameraCard;
