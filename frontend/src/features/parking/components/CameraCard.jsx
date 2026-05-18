import React, { useRef, useState } from "react";
import { Card, Button, Space, Tooltip, Spin, Select } from "antd";
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
import { useNotification } from "../../../hooks/useNotification";
import ConfirmModal from "./ConfirmModal";

const CameraCard = ({
  title,
  type,
  laneId,
  videoSrc,
  onSuccess,
  vehicleType = "MOTOR",
}) => {
  const notify = useNotification();
  const imgRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [detectedPlate, setDetectedPlate] = useState(null);
  const [imgError, setImgError] = useState(false);
  const [zoom, setZoom] = useState(1);
  const [localVehicleType, setLocalVehicleType] = useState(vehicleType || "MOTOR");
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [modalData, setModalData] = useState(null);
  const themeColor = "#141414";
  const normalizeUuid = (value) => {
    if (!value) {
      return "";
    }

    return String(value).replace(/^"|"$/g, "").trim();
  };

  const handleZoom = () => {
    const newZoom = zoom >= 2 ? 1 : zoom + 0.5;
    setZoom(newZoom);
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
      notify.error("Hình ảnh chưa sẵn sàng");
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
      notify.error("Không thể chụp ảnh");
      return;
    }
    const file = new File([blob], `capture_${Date.now()}.jpg`, {
      type: "image/jpeg",
    });

    setLoading(true);
    try {
      const normalizedLaneId = normalizeUuid(laneId);
      if (!normalizedLaneId) {
        notify.error("Thieu thong tin lane, vui long chon lai lane.");
        return;
      }

      const normalizedVehicleType = String(localVehicleType || "MOTOR")
        .toUpperCase()
        .trim();

      if (type === "IN" && !["MOTOR", "CAR", "BICYCLE"].includes(normalizedVehicleType)) {
        notify.error("vehicleType khong hop le. Chi chap nhan MOTOR, CAR hoac BICYCLE.");
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
        notify.error("Thiếu parkingSessionId. Hãy check-in thành công trước khi check-out.");
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

      // Trong handleCaptureAndSend, sau response thành công
      if (response?.success) {
        const plate = response?.data?.plateInOcr || "Đã nhận diện";
        setDetectedPlate(plate);

        // Truyền toàn bộ data từ response + entryLaneId
        setModalData({
          ...response.data,   // plateInOcr, imageInUrl, timeIn, confidenceIn, vehicleType
          entryLaneId: normalizedLaneId,
        });
        setShowConfirmModal(true);
      } else {
        notify.error(response?.message || "Lỗi từ server");
      }
    } catch (error) {
      console.error(error);
      notify.apiError(error, "Gửi ảnh thất bại");
    } finally {
      setLoading(false);
    }
  };

  const handleModalConfirmed = (confirmedData) => {
    if (confirmedData?.id) {
      saveActiveParkingSessionId(confirmedData.id);
    }

    const plate = confirmedData?.finalPlate || confirmedData?.plateNumber || "Da nhan dien";
    notify.success(
      `Biển số xe: ${plate}`,
      "Check-in Thành Công"
    );
    setShowConfirmModal(false);
    setModalData(null);

    if (onSuccess) {
      onSuccess(confirmedData);
    }
  };

  const handleModalCancel = () => {
    setShowConfirmModal(false);
    setModalData(null);
  };

  return (
    <>
      <ConfirmModal
        visible={showConfirmModal}
        initialData={modalData}
        onCancel={handleModalCancel}
        onConfirmed={handleModalConfirmed}
      />
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
            <span style={{ color: "#141414", fontWeight: 600, letterSpacing: 1 }}>{title}</span>
          </div>
        }
        extra={
          <Space size="small">
            {type === "IN" && (
              <Select
                value={localVehicleType}
                onChange={setLocalVehicleType}
                options={[
                  { label: "Xe máy", value: "MOTOR" },
                  { label: "Ô tô", value: "CAR" },
                  { label: "Xe đạp", value: "BICYCLE" },
                ]}
                style={{ width: 100 }}
              />
            )}
            <Tooltip title="Chụp và gửi" color="#141414" overlayStyle={{ color: "#fff" }}>
              <Button
                type="primary"
                shape="circle"
                icon={<CameraOutlined />}
                onClick={handleCaptureAndSend}
                loading={loading}
                style={{ backgroundColor: "#0eb1a3" }}
              />
            </Tooltip>
            <Tooltip title="Phóng to" color="#141414" overlayStyle={{ color: "#fff" }}>
              <Button
                type="text"
                shape="circle"
                icon={<ZoomInOutlined style={{ color: "#141414" }} />}
                onClick={handleZoom}
              />
            </Tooltip>
          </Space>
        }
        variant="borderless"
        style={{
          background: "#ffffff",
          border: "1px solid #e5e7eb",
          borderRadius: 16,
          boxShadow: "0 14px 30px rgba(15, 23, 42, 0.12)",
          overflow: "hidden",
        }}
        styles={{
          header: { borderBottom: "1px solid #e5e7eb", padding: "12px 16px" },
          body: { padding: 12 },
        }}
      >
        <div
          style={{
            height: 450,
            borderRadius: 8,
            overflow: "hidden",
            position: "relative",
            background: "#f5f5f5",
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
                background: "#ffffff",
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
              style={{
                width: "100%",
                height: "100%",
                objectFit: "cover",
                transform: `scale(${zoom})`,
                transition: "transform 0.3s ease"
              }}
              onError={() => {
                setImgError(true);
                notify.error("Loi ket noi camera. Kiem tra IP DroidCam.");
              }}
              onLoad={() => setImgError(false)}
              crossOrigin="anonymous"
            />
          )}
          <div
            style={{
              position: "absolute",
              inset: 0,
              background: "linear-gradient(to top, rgba(0,0,0,0.3) 0%, transparent 40%)",
              pointerEvents: "none",
            }}
          />
          <div
            style={{
              position: "absolute",
              top: 12,
              left: 12,
              background: "rgba(20,20,20,0.7)",
              backdropFilter: "blur(4px)",
              padding: "4px 10px",
              borderRadius: 4,
            }}
          >
            <span style={{ color: "#fff", fontSize: 10, fontWeight: 700 }}>LIVE</span>
          </div>
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
    </>
  );
};

export default CameraCard;
