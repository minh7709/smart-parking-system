import React, { useRef, useState } from "react";
import { Card, Button, Space, Tooltip, message, Spin } from "antd";
import {
  CameraOutlined,
  ZoomInOutlined,
  SettingOutlined,
  ExpandOutlined,
} from "@ant-design/icons";
import { checkInApi, checkOutApi } from "../api/parkingSession.api";
import {
  getAccessToken,
  getActiveParkingSessionId,
  saveActiveParkingSessionId,
  clearActiveParkingSessionId,
} from "../../../utils/storage";

const CameraCard = ({
  title,
  type,
  laneId,
  videoSrc,
  onSuccess,
  vehicleType = "MOTOR",
}) => {
  const imgRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [detectedPlate, setDetectedPlate] = useState(null);
  const [imgError, setImgError] = useState(false);
  const themeColor = "#ffffff";
  const normalizeUuid = (value) => {
    if (!value) {
      return "";
    }

    return String(value).replace(/^"|"$/g, "").trim();
  };

  const buildAuthRequestOptions = () => {
    const rawToken = getAccessToken();
    if (!rawToken) {
      return {};
    }

    const normalizedToken = String(rawToken).replace(/^Bearer\s+/i, "").trim();
    if (!normalizedToken) {
      return {};
    }

    return {
      headers: {
        Authorization: `Bearer ${normalizedToken}`,
      },
    };
  };

  const handleCaptureAndSend = async () => {
    const img = imgRef.current;
    if (!img || !img.complete || img.naturalWidth === 0) {
      message.error("Hình ảnh chưa sẵn sàng");
      return;
    }

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
      const normalizedLaneId = normalizeUuid(laneId);
      if (!normalizedLaneId) {
        message.error("Thieu thong tin lane, vui long chon lai lane.");
        return;
      }

      const normalizedVehicleType = String(vehicleType || "MOTOR")
        .toUpperCase()
        .trim();

      if (type === "IN" && !["MOTOR", "CAR"].includes(normalizedVehicleType)) {
        message.error("vehicleType khong hop le. Chi chap nhan MOTOR hoac CAR.");
        return;
      }

      const requestPayload =
        type === "IN"
          ? {
              entryLaneId: normalizedLaneId,
              vehicleType: normalizedVehicleType,
            }
          : {
              exitLaneId: normalizedLaneId,
              parkingSessionId: normalizeUuid(getActiveParkingSessionId()),
            };

      if (type === "OUT" && !requestPayload.parkingSessionId) {
        message.error("Thiếu parkingSessionId. Hãy check-in thành công trước khi check-out.");
        return;
      }

      const formData = new FormData();
      formData.append(
        "request",
        new Blob([JSON.stringify(requestPayload)], {
          type: "application/json;charset=UTF-8",
        }),
        "request.json",
      );
      formData.append("image", file);

      const requestOptions = buildAuthRequestOptions();

      const response =
        type === "IN"
          ? await checkInApi(formData, requestOptions)
          : await checkOutApi(formData, requestOptions);

      if (response?.success) {
        const plate =
          response?.data?.plateNumber ||
          response?.data?.finalPlate ||
          response?.data?.plateInOcr ||
          response?.data?.plateOutOcr ||
          "Da nhan dien";

        if (type === "IN" && response?.data?.id) {
          saveActiveParkingSessionId(response.data.id);
        }

        if (type === "OUT") {
          clearActiveParkingSessionId();
        }

        setDetectedPlate(plate);
        message.success(`${type === "IN" ? "Check-in" : "Check-out"} thành công: ${plate}`);
        if (onSuccess) {
          onSuccess(response.data);
        }
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
          <span style={{ color: "#fff", fontWeight: 600, letterSpacing: 1 }}>{title}</span>
        </div>
      }
      extra={
        <Space size="small">
          <Tooltip title="Chup va gui">
            <Button
              type="primary"
              shape="circle"
              icon={<CameraOutlined />}
              onClick={handleCaptureAndSend}
              loading={loading}
              style={{ backgroundColor: "#00b96b" }}
            />
          </Tooltip>
          <Tooltip title="Phong to">
            <Button
              type="text"
              shape="circle"
              icon={<ZoomInOutlined style={{ color: "#fff" }} />}
            />
          </Tooltip>
          <Tooltip title="Cai dat luong">
            <Button
              type="text"
              shape="circle"
              icon={<SettingOutlined style={{ color: "#fff" }} />}
            />
          </Tooltip>
          <Tooltip title="Mo toan man hinh">
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
              Khong the ket noi camera.
              <br />
              Kiem tra IP DroidCam
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
              message.error("Loi ket noi camera. Kiem tra IP DroidCam.");
            }}
            onLoad={() => setImgError(false)}
            crossOrigin="anonymous"
          />
        )}
        <div
          style={{
            position: "absolute",
            inset: 0,
            background: "linear-gradient(to top, rgba(0,0,0,0.9) 0%, transparent 40%)",
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
          <span style={{ color: "#fff", fontSize: 10, fontWeight: 700 }}>LIVE</span>
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
