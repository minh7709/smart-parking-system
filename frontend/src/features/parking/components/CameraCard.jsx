import React, { useMemo, useRef, useState } from "react";
import { Card, Button, Space, Tooltip, Spin, Select, Input, Modal, Upload } from "antd";
import {
  CameraOutlined,
  ZoomInOutlined,
} from "@ant-design/icons";
import { checkInApi, checkOutApi, reportIncidentApi, reportLostCardApi } from "../api/parkingSession.api";
import {
  getAccessToken,
  getActiveParkingSessionId,
  getSystemTypes
} from "../../../utils/storage";
import { useNotification } from "../../../hooks/useNotification";


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
  const [imgError, setImgError] = useState(false);
  const [zoom, setZoom] = useState(1);
  const [localVehicleType, setLocalVehicleType] = useState(vehicleType || "MOTOR");
  const [exitSessionId, setExitSessionId] = useState("");
  const [incidentType, setIncidentType] = useState("");
  const [incidentModalOpen, setIncidentModalOpen] = useState(false);
  const [incidentDescription, setIncidentDescription] = useState("");
  const [incidentSubmitting, setIncidentSubmitting] = useState(false);
  const [incidentEvidenceFile, setIncidentEvidenceFile] = useState(null);
  const themeColor = "#141414";
 /*const SYSTEM_TYPE_KEYS = [
  "laneStatuses",
  "laneTypes",
  "vehicleTypes",
  "sessionStatuses",
  "paymentStatuses",
  "paymentMethods",
  "pricingStrategies",
  "incidentTypes",
  "userRoles",
  "userStatuses",
  "subscriptionTypes",
  "subscriptionStatuses"
]; */
  const vehicleTypeOptions = useMemo(() => {
    const vehicleTypes = getSystemTypes('vehicleTypes') ?? [];

    if (Array.isArray(vehicleTypes) && vehicleTypes.length > 0) {
      return vehicleTypes.map((item) => {
        const value = String(item.value || "").toUpperCase();
        const label = item.label || "Unknown";
        return { label, value };
      }).filter((option) => option.value);
    }

    return [
      { label: "Xe máy", value: "MOTOR" },
      { label: "Ô tô", value: "CAR" },
      { label: "Xe đạp", value: "BICYCLE" },
    ];
  }, []);
  const incidentTypeOptions = useMemo(() => {
    const incidentTypes = getSystemTypes('incidentTypes') ?? [];
    if (Array.isArray(incidentTypes) && incidentTypes.length > 0) {
      return incidentTypes.map((item) => {
        const value = String(item.value || "").toUpperCase();
        const label = item.label || "Unknown";
        return { label, value };
      }).filter((option) => option.value);
    }

    return [
      { label: "Mất thẻ", value: "LOST_CARD" },
      { label: "Va chạm", value: "DAMAGE" },
      { label: "Lỗi hệ thống", value: "SYSTEM_ERROR" },
      { label: "Chụp sai biển số", value: "WRONG_PLATE" },
      { label: "Khác", value: "OTHER" },
    ];
  }, []);
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

  const captureImageFile = async () => {
    const img = imgRef.current;
    if (!img || !img.complete || img.naturalWidth === 0) {
      notify.error("Hình ảnh chưa sẵn sàng");
      return null;
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
      return null;
    }

    return new File([blob], `capture_${Date.now()}.jpg`, {
      type: "image/jpeg",
    });
  };

  const handleCaptureAndSend = async () => {
    const file = await captureImageFile();
    if (!file) {
      return;
    }

    setLoading(true);
    try {
      const normalizedLaneId = normalizeUuid(laneId);
      if (!normalizedLaneId) {
        notify.error("Thiếu thông tin làn, vui lòng chọn lại làn.");
        return;
      }

      const normalizedVehicleType = String(localVehicleType || "MOTOR")
        .toUpperCase()
        .trim();

      if (type === "IN" && !["MOTOR", "CAR", "BICYCLE"].includes(normalizedVehicleType)) {
        notify.error("Loại xe không hợp lệ. Chỉ chấp nhận MOTOR, CAR hoặc BICYCLE.");
        return;
      }

      const normalizedSessionId =
        type === "OUT"
          ? normalizeUuid(exitSessionId) || normalizeUuid(getActiveParkingSessionId())
          : "";

      const requestPayload =
        type === "IN"
          ? {
              entryLaneId: normalizedLaneId,
              vehicleType: normalizedVehicleType,
            }
          : {
              exitLaneId: normalizedLaneId,
              parkingSessionId: normalizedSessionId,
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
        if (onSuccess) {
          onSuccess({
            ...response.data,
            entryLaneId: normalizedLaneId,
          });
        }
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

  const handleIncidentTypeChange = (value) => {
    setIncidentType(value || "");
    if (value) {
      setIncidentModalOpen(true);
    }
  };

  const handleEvidenceChange = (info) => {
    const file = info?.file?.originFileObj || info?.file || null;
    setIncidentEvidenceFile(file || null);
  };

  const handleReportIncident = async () => {
    if (!incidentType) {
      notify.error("Vui lòng chọn loại sự cố.");
      return;
    }

    if (!incidentEvidenceFile) {
      notify.error("Vui lòng tải ảnh bằng chứng.");
      return;
    }

    const normalizedSessionId = normalizeUuid(exitSessionId) || normalizeUuid(getActiveParkingSessionId());
    const normalizedLaneId = normalizeUuid(laneId);

    setIncidentSubmitting(true);
    try {
      const formData = new FormData();
      const incidentCode = String(incidentType).toUpperCase().trim();

      if (incidentCode === "LOST_CARD") {
        if (!normalizedLaneId) {
          notify.error("Thiếu mã làn. Vui lòng chọn lại làn.");
          return;
        }
        const file = await captureImageFile();
        if (!file) {
          return;
        }
        const requestPayload = {
          exitLaneId: normalizedLaneId,
          description: incidentDescription || "",
        };
        formData.append(
          "request",
          new Blob([JSON.stringify(requestPayload)], {
            type: "application/json;charset=UTF-8",
          }),
          "request.json",
        );
        formData.append("image", file);
        formData.append("evidenceImage", incidentEvidenceFile);
      } else {
        if (!normalizedSessionId) {
          notify.error("Thiếu parkingSessionId. Hãy nhập hoặc check-in trước.");
          return;
        }
        const requestPayload = {
          parkingSessionId: normalizedSessionId,
          description: incidentDescription || "",
          incidentType: incidentCode,
        };
        formData.append(
          "request",
          new Blob([JSON.stringify(requestPayload)], {
            type: "application/json;charset=UTF-8",
          }),
          "request.json",
        );
        formData.append("evidenceImage", incidentEvidenceFile);
      }

      const response =
        incidentCode === "LOST_CARD"
          ? await reportLostCardApi(formData, buildAuthRequestOptions())
          : await reportIncidentApi(formData, buildAuthRequestOptions());
      if (response?.success) {
        notify.success("Báo sự cố thành công");
        setIncidentModalOpen(false);
        setIncidentDescription("");
        setIncidentType("");
        setIncidentEvidenceFile(null);
      } else {
        notify.error(response?.message || "Lỗi từ server");
      }
    } catch (error) {
      console.error(error);
      notify.apiError(error, "Báo sự cố thất bại");
    } finally {
      setIncidentSubmitting(false);
    }
  };

  return (
    <>
      <div style={{ marginLeft: 10, marginBottom: 10}}>
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
          <span style={{ color: "#1700c3", fontWeight: 800, letterSpacing: 1 }}>{title}</span>
        </div>
      </div>
      <Card
        extra={
          <Space size="small">
            {type === "IN" && (
              <Select
                value={localVehicleType}
                onChange={setLocalVehicleType}
                options={vehicleTypeOptions}
                style={{ width: 100 }}
              />
            )}
            {type === "OUT" && (
              <Select
                value={incidentType || undefined}
                onChange={handleIncidentTypeChange}
                options={incidentTypeOptions}
                placeholder="Báo sự cố"
                allowClear
                style={{ width: 160 }}
              />
            )}
            {type === "OUT" && (
              <Input
                value={exitSessionId}
                onChange={(event) => setExitSessionId(event.target.value)}
                placeholder="Parking session ID"
                style={{ width: 300 }}
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
                Không thể kết nối camera.
                <br />
                Vui lòng kiểm tra lại IP của lane.
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
      <Modal
        title="Báo sự cố"
        open={incidentModalOpen}
        onCancel={() => setIncidentModalOpen(false)}
        onOk={handleReportIncident}
        okText="Gửi"
        cancelText="Hủy"
        okButtonProps={{ loading: incidentSubmitting }}
      >
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 6 }}>
              Parking session ID
            </div>
            <Input
              value={exitSessionId}
              onChange={(event) => setExitSessionId(event.target.value)}
              placeholder="Parking session ID"
            />
          </div>
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 6 }}>
              Mô tả
            </div>
            <Input.TextArea
              value={incidentDescription}
              onChange={(event) => setIncidentDescription(event.target.value)}
              placeholder="Mô tả chi tiết về sự cố (không bắt buộc)"
              rows={4}
            />
          </div>
          <div>
            <div style={{ fontSize: 12, fontWeight: 600, marginBottom: 6 }}>
              Ảnh bằng chứng
            </div>
            <Upload
              accept="image/*"
              maxCount={1}
              beforeUpload={() => false}
              onChange={handleEvidenceChange}
              onRemove={() => setIncidentEvidenceFile(null)}
            >
              <Button>Chọn ảnh</Button>
            </Upload>
          </div>
        </div>
      </Modal>
    </>
  );
};

export default CameraCard;
