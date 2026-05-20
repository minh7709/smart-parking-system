import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { Row, Col, Alert } from "antd";
import { AppLayout } from "../../../components/Layout/AppLayout";
import CameraCard from "../components/CameraCard";
import ParkedTable from "../components/ParkedTable";
import HistoryTable from "../components/HistoryTable";
import ConfirmModal from "../components/ConfirmModal";
import ConfirmCheckOutModal from "../components/ConfirmCheckOutModal";
import { getLaneSelection, saveActiveParkingSessionId } from "../../../utils/storage";

const MonitorPage = () => {
  const location = useLocation();
  const savedSelection = getLaneSelection();
  const parkedTableRef = React.useRef();

  const checkInLane = location.state?.checkInLane || savedSelection.checkInLane;
  const checkOutLane =
    location.state?.checkOutLane || savedSelection.checkOutLane;

  // THAY IP CỦA BẠN VÀO ĐÂY
  // Cách 1: dùng IP từ lane (nếu có)
  // const cameraInUrl = checkInLane?.ipCamera
  //   ? `http://${checkInLane.ipCamera}:4747/video`
  //   : null;

  // Cách 2: dùng IP cứng để test (chú ý: IP này phải đúng và reachable)
  const cameraInUrl = "/camera-in-stream";
  const cameraOutUrl = "/camera-out-stream";



  const [cameraStatus, setCameraStatus] = useState("checking");
  const [pendingConfirm, setPendingConfirm] = useState(null);
  const [pendingConfirmOut, setPendingConfirmOut] = useState(null);

  if (!cameraInUrl) {
    return (
      <AppLayout>
        <Alert
          message="Chưa chọn lane vào"
          description="Vui lòng quay lại trang Lane để chọn lane có camera."
          type="warning"
          showIcon
        />
      </AppLayout>
    );
  }

  return (
    <div style={{ backgroundColor: '#f3f4f6', minHeight: '100vh', padding: '24px' }}>
      {cameraStatus === "error" && (
        <Alert
          message="Không kết nối được camera"
          description={`Không thể truy cập ${cameraInUrl}. Hãy kiểm tra IP DroidCam và đảm bảo điện thoại cùng mạng WiFi.`}
          type="error"
          showIcon
          closable
          style={{ marginBottom: 16 }}
        />
      )}
      <Row gutter={24} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <CameraCard
            type="IN"
            title={checkInLane?.laneName || "LÀN VÀO"}
            laneId={checkInLane?.id}
            vehicleType="MOTOR"
            videoSrc={cameraInUrl}
            onSuccess={(data) => {
              setPendingConfirm({ ...data, entryLaneId: checkInLane?.id });
            }}
          />
        </Col>
        <Col span={12}>
          <CameraCard
            type="OUT"
            title={checkOutLane?.laneName || "LÀN RA"}
            laneId={checkOutLane?.id}
            vehicleType="MOTO"
            videoSrc={cameraOutUrl}
            onSuccess={(data) => {
              setPendingConfirmOut({
                ...data,
                exitLaneId: checkOutLane?.id,
              });
            }}
          />
        </Col>
      </Row>
      <div style={{ marginTop: 20 }}>
        <ParkedTable ref={parkedTableRef} />
      </div>
      <div style={{ marginTop: 20 }}>
        <HistoryTable />
      </div>

      <ConfirmModal
        visible={!!pendingConfirm}
        initialData={pendingConfirm}
        onClose={() => setPendingConfirm(null)}
        onConfirmed={(confirmed) => {
          // after confirm, clear modal and refresh parked table
          setPendingConfirm(null);
          if (confirmed?.id) {
            saveActiveParkingSessionId(confirmed.id);
          }
          console.log('confirm-check-in result', confirmed);
          parkedTableRef.current?.refresh();
        }}
      />

      <ConfirmCheckOutModal
        visible={!!pendingConfirmOut}
        initialData={pendingConfirmOut}
        onClose={() => setPendingConfirmOut(null)}
        onConfirmed={() => {
          setPendingConfirmOut(null);
          parkedTableRef.current?.refresh();
        }}
      />
    </div>
  );
};

export default MonitorPage;
