import React, { useState } from 'react';
import { Modal, Input, Image, Row, Col, Button, message } from 'antd';
import { confirmCheckInApi } from '../api/parkingSession.api';

const ConfirmModal = ({ visible, initialData, onCancel, onConfirmed }) => {
  const [plate, setPlate] = useState(initialData?.plateNumber || initialData?.finalPlate || '');
  const [loading, setLoading] = useState(false);

  // keep plate in sync when initialData changes
  React.useEffect(() => {
    setPlate(initialData?.plateNumber || initialData?.finalPlate || '');
  }, [initialData]);

  const handleConfirm = async () => {
    if (!initialData || !initialData.id) {
      message.error('Thiếu thông tin phiên check-in');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        entryLaneId: initialData.entryLaneId,
        finalPlate: (plate || '').toString().trim(),
        parkingSessionId: initialData.id,
      };

      const resp = await confirmCheckInApi(payload);
      if (resp?.success) {
        message.success('Xác nhận check-in thành công');
        if (onConfirmed) onConfirmed(resp.data);
      } else {
        message.error(resp?.message || 'Xác nhận thất bại');
      }
    } catch (err) {
      console.error(err);
      message.error(err?.message || 'Lỗi khi gọi API');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      title="Xác nhận Check-in"
      open={!!visible}
      onCancel={onCancel}
      footer={[
        <Button key="cancel" onClick={onCancel} disabled={loading}>
          Hủy
        </Button>,
        <Button key="confirm" type="primary" loading={loading} onClick={handleConfirm}>
          Xác nhận
        </Button>,
      ]}
    >
      <Row gutter={12}>
        <Col span={12}>
          {initialData?.imageUrl ? (
            <Image src={initialData.imageUrl} alt="capture" style={{ width: '100%' }} />
          ) : (
            <div style={{ width: '100%', height: 160, background: '#f0f0f0' }} />
          )}
        </Col>
        <Col span={12}>
          <div style={{ marginBottom: 8, fontSize: 12, color: '#888' }}>OCR</div>
          <Input value={plate} onChange={(e) => setPlate(e.target.value)} />
          {initialData?.confidence !== undefined && (
            <div style={{ marginTop: 8, fontSize: 12, color: '#666' }}>
              Confidence: {initialData.confidence}
            </div>
          )}
        </Col>
      </Row>
    </Modal>
  );
};

export default ConfirmModal;
