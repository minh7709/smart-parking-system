import React, { useState } from 'react';
import { Modal, Input, Image, Row, Col, Button, message, notification } from 'antd';
import { confirmCheckInApi } from '../api/parkingSession.api';

const ConfirmModal = ({ visible, initialData, onCancel, onConfirmed }) => {
  const [plate, setPlate] = useState(initialData?.plateNumber || initialData?.finalPlate || initialData?.plateInOcr || '');
  const [loading, setLoading] = useState(false);
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);

  // keep plate in sync when initialData changes
  React.useEffect(() => {
    setPlate(initialData?.plateNumber || initialData?.finalPlate || initialData?.plateInOcr || '');
  }, [initialData]);

  const handleConfirm = async () => {
    if (!initialData || !initialData.id) {
      message.error('Thiếu thông tin phiên check-in');
      return;
    }

    if (!plate.trim()) {
      message.error('Vui lòng nhập biển số');
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
        notification.success({
          message: ' Check-in thành công',
          description: `Biển số: ${plate}`,
          placement: 'topRight',
          duration: 3,
        });
        setTimeout(() => {
          if (onConfirmed) onConfirmed(resp.data);
        }, 500);
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

  const handleCancelClick = () => {
    notification.info({
      message: 'Đã hủy',
      description: 'Bạn đã hủy xác nhận check-in',
      placement: 'topRight',
      duration: 2,
    });
    onCancel();
  };

  return (
    <Modal
      title="Xác nhận Check-in"
      open={!!visible}
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
            {initialData?.imageUrl ? (
              <Image src={initialData.imageUrl} alt="capture" style={{ width: '100%' }} />
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
              {initialData?.plateInOcr || initialData?.plateNumber || 'N/A'}
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
            
            {initialData?.confidence !== undefined && (
              <div style={{ marginTop: 12, fontSize: 12, color: '#999' }}>
                Độ tin cậy: {(initialData.confidence * 100).toFixed(0)}%
              </div>
            )}
          </Col>
        </Row>
      </Modal>
    );
  };
  
  export default ConfirmModal;
