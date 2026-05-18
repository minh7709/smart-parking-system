import React, { useState, useEffect } from 'react';
import { Modal, Input, Image, Row, Col, Button } from 'antd';
import { confirmCheckInApi } from '../api/parkingSession.api';
import { useNotification } from '../../../hooks/useNotification';

const ConfirmModal = ({ visible, initialData, onCancel, onConfirmed }) => {
  const notify = useNotification();
  const [plate, setPlate] = useState('');
  const [loading, setLoading] = useState(false);

  // Cập nhật plate khi initialData thay đổi
  useEffect(() => {
    if (initialData?.plateInOcr) {
      setPlate(initialData.plateInOcr);
    }
  }, [initialData]);

  const handleConfirm = async () => {
    if (!initialData) {
      notify.error('Thiếu thông tin từ check-in');
      return;
    }

    const trimmedPlate = plate?.trim();
    if (!trimmedPlate) {
      notify.error('Vui lòng nhập biển số');
      return;
    }

    setLoading(true);
    try {
      // Tạo payload đúng theo yêu cầu của confirm API
      const payload = {
        finalPlate: trimmedPlate,
        plateInOcr: initialData.plateInOcr,
        imageInUrl: initialData.imageInUrl,
        timeIn: initialData.timeIn,
        confidenceIn: initialData.confidenceIn,
        vehicleType: initialData.vehicleType,
        entryLaneId: initialData.entryLaneId,
      };

      const resp = await confirmCheckInApi(payload);
      if (resp?.success) {
        notify.success(`Biển số: ${trimmedPlate}`, 'Check-in thành công');
        // Gọi callback với dữ liệu trả về (chứa parkingSessionId)
        if (onConfirmed) onConfirmed(resp.data);
      } else {
        notify.error(resp?.message || 'Xác nhận thất bại');
      }
    } catch (err) {
      console.error(err);
      notify.apiError(err, 'Lỗi khi gọi API');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelClick = () => {
    notify.info('Bạn đã hủy xác nhận check-in', 'Đã hủy');
    onCancel();
  };

  return (
    <Modal
      title="Xác nhận Check-in"
      open={visible}
      onCancel={handleCancelClick}
      footer={[
        <Button key="cancel" onClick={handleCancelClick} disabled={loading}>
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
          {initialData?.imageInUrl ? (
            <Image src={initialData.imageInUrl} alt="capture" style={{ width: '100%' }} />
          ) : (
            <div style={{ width: '100%', height: 160, background: '#f0f0f0' }} />
          )}
        </Col>
        <Col span={12}>
          <div style={{ marginBottom: 8, fontSize: 12, color: '#666' }}>
            Biển số AI đọc được:
          </div>
          <div style={{
            marginBottom: 16,
            fontSize: 16,
            fontWeight: 'bold',
            fontFamily: 'monospace',
            padding: '8px 12px',
            background: '#e6f7ff',
            borderRadius: 4,
            border: '1px solid #91d5ff',
            color: '#0050b3',
          }}>
            {initialData?.plateInOcr || 'N/A'}
          </div>

          <div style={{ marginBottom: 8, fontSize: 12, color: '#666' }}>
            Nhập/Sửa biển số thực tế:
          </div>
          <Input
            value={plate}
            onChange={(e) => setPlate(e.target.value)}
            placeholder="Nhập biển số..."
            size="large"
          />

          {initialData?.confidenceIn !== undefined && (
            <div style={{ marginTop: 12, fontSize: 12, color: '#999' }}>
              Độ tin cậy: {(initialData.confidenceIn * 100).toFixed(0)}%
            </div>
          )}
        </Col>
      </Row>
    </Modal>
  );
};

export default ConfirmModal;