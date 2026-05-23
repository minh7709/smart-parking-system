import React, { useEffect, useMemo, useState } from "react";
import { Modal, Image, Row, Col, Button, Select } from "antd";
import {
  confirmCheckOutApi,
  getParkingSessionImageApi,
  getParkingSessionImageByUrlApi,
} from "../api/parkingSession.api";
import { getSystemTypes } from "../../../utils/storage";
import { useNotification } from "../../../hooks/useNotification";

const formatDateTime = (value) => {
  if (!value) {
    return "N/A";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return date.toLocaleString();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || value === "") {
    return "N/A";
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return String(value);
  }
  return `${numeric.toLocaleString("vi-VN")} VND`;
};

const ConfirmCheckOutModal = ({ visible, initialData, onClose, onConfirmed }) => {
  const notify = useNotification();
  const [loading, setLoading] = useState(false);
  const [imageSrc, setImageSrc] = useState(null);
  const [imageLoading, setImageLoading] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState(null);

  const paymentMethods = getSystemTypes('paymentMethods') ?? [];
  const vehicleTypes = getSystemTypes('vehicleTypes') ?? [];

  const paymentOptions = useMemo(() => {
    if (Array.isArray(paymentMethods) && paymentMethods.length > 0) {
      return paymentMethods.map((item) => ({
        value: item.value,
        label: item.label
      }));
    }

    return [
      { label: "Tiền mặt", value: "CASH" },
      { label: "Chuyển khoản", value: "ONLINE_PAYMENT" },
    ];
  }, [paymentMethods]);

  const vehicleTypeLabel = useMemo(() => {
    const rawType = initialData?.vehicleType?.value;
    return rawType;
  }, [initialData?.vehicleType]);

  useEffect(() => {
    if (!paymentMethod && paymentOptions.length > 0) {
      setPaymentMethod(paymentOptions[0].value);
    }
  }, [paymentMethod, paymentOptions]);

  useEffect(() => {
    let isActive = true;
    let objectUrl = null;

    const fetchImage = async () => {
      if (!initialData?.id) {
        setImageSrc(null);
        return;
      }

      setImageLoading(true);
      try {
        const blob = await getParkingSessionImageByUrlApi(initialData.imageOutUrl);
        objectUrl = URL.createObjectURL(blob);
        if (isActive) {
          setImageSrc(objectUrl);
        }
      } catch (err) {
        console.error(err);
        if (isActive) {
          setImageSrc(null);
          notify.apiError(err, "Lỗi khi tải ảnh check-out");
        }
      } finally {
        if (isActive) {
          setImageLoading(false);
        }
      }
    };

    fetchImage();

    return () => {
      isActive = false;
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [initialData?.id, notify]);

  const handleConfirm = async () => {
    if (!initialData?.id) {
      notify.error("Dữ liệu không hợp lệ. Vui lòng thử lại.");
      return;
    }

    if (!paymentMethod) {
      notify.error("Vui lòng chọn phương thức thanh toán");
      return;
    }

    if (onClose) {
      onClose();
    }
    setLoading(true);
    try {
      const payload = {
        parkingSessionId: initialData.id,
        paymentMethod,
        parkingAmount: initialData.parkingAmount || 0,
        penaltyAmount: initialData.penaltyAmount || 0,
        imageOutUrl: initialData.imageOutUrl || null,
        confidenceOut: initialData.confidenceOut || null,
        exitLaneId: initialData.exitLaneId || null,
        timeOut: initialData.timeOut || null,
        relatedSessionIds: initialData.relatedSessionIds || []
      };
      const resp = await confirmCheckOutApi(payload);
      if (resp?.success) {
        notify.success("Check-out thành công");
        if (onConfirmed) {
          onConfirmed();
        }
      } else {
        notify.error(resp?.message || "Xác nhận check-out thất bại");
      }
    } catch (err) {
      console.error(err);
      notify.apiError(err, "Lỗi khi xác nhận check-out");
    } finally {
      setLoading(false);
    }
  };

  const plateValue = initialData?.finalPlate || initialData?.plateOutOcr || "N/A";

  return (
    <Modal
      title="Xác nhận check-out"
      open={visible}
      onCancel={onClose}
      footer={[
        <Button key="cancel" onClick={onClose} disabled={loading}>
          Hủy
        </Button>,
        <Button key="confirm" type="primary" loading={loading} onClick={handleConfirm}>
            Xác nhận
        </Button>,
      ]}
      maskClosable={false}
    >
      <Row gutter={12}>
        <Col span={12}>
          {imageSrc ? (
            <Image src={imageSrc} alt="check-out" style={{ width: "100%" }} />
          ) : (
            <div style={{ width: "100%", height: 160, background: "#f0f0f0" }}>
              {imageLoading ? "Đang tải ảnh..." : null}
            </div>
          )}
        </Col>
        <Col span={12}>
          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Biển số xe:</div>
          <div
            style={{
              marginBottom: 12,
              fontSize: 16,
              fontWeight: "bold",
              fontFamily: "monospace",
              padding: "8px 12px",
              background: "#fff7e6",
              borderRadius: 4,
              border: "1px solid #ffd591",
              color: "#ad4e00",
            }}
          >
            {plateValue}
          </div>

          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Loại xe:</div>
          <div style={{ marginBottom: 12, fontWeight: 600 }}>{vehicleTypeLabel}</div>

          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Thời gian vào:</div>
          <div style={{ marginBottom: 12 }}>{formatDateTime(initialData?.timeIn)}</div>

          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Thời gian ra:</div>
          <div style={{ marginBottom: 12 }}>{formatDateTime(initialData?.timeOut)}</div>

          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Số tiền:</div>
          <div style={{ marginBottom: 12, fontWeight: 600 }}>{formatCurrency(initialData?.parkingAmount + initialData?.penaltyAmount)}</div>

          <div style={{ marginBottom: 8, fontSize: 12, color: "#666" }}>Phương thức thanh toán:</div>
          <Select
            value={paymentMethod}
            onChange={setPaymentMethod}
            options={paymentOptions}
            style={{ width: "100%" }}
            placeholder="Chọn PTTT"
          />
        </Col>
      </Row>
    </Modal>
  );
};

export default ConfirmCheckOutModal;
