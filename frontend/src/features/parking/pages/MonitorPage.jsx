import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { Row, Col, Alert } from "antd";
import { AppLayout } from "../../../components/Layout/AppLayout";
import CameraCard from "../components/CameraCard";
import HistoryTable from "../components/HistoryTable";
import Stats from "../components/Stats";
import { getLaneSelection } from "../../../utils/storage";

const MonitorPage = () => {
  const location = useLocation();
  const savedSelection = getLaneSelection();

  const checkInLane = location.state?.checkInLane || savedSelection.checkInLane;
  const checkOutLane =
    location.state?.checkOutLane || savedSelection.checkOutLane;

  // THAY IP CỦA BẠN VÀO ĐÂY
  // Cách 1: dùng IP từ lane (nếu có)
  // const cameraInUrl = checkInLane?.ipCamera
  //   ? `http://${checkInLane.ipCamera}:4747/video`
  //   : null;

  // Cách 2: dùng IP cứng để test (chú ý: IP này phải đúng và reachable)
  const cameraInUrl = "http://192.168.14.201:4747/video";

  const [cameraStatus, setCameraStatus] = useState("checking");

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
    <AppLayout>
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
            vehicleType="MOTO"
            videoSrc={cameraInUrl}
            onSuccess={(data) => console.log("Check-in success", data)}
          />
        </Col>
        <Col span={12}>
          <CameraCard
            type="OUT"
            title={checkOutLane?.laneName || "LÀN RA"}
            laneId={checkOutLane?.id}
            vehicleType="MOTO"
            videoSrc={cameraInUrl} // tạm thời dùng chung, sau này thay bằng camera out riêng
            onSuccess={(data) => console.log("Check-out success", data)}
          />
        </Col>
      </Row>
      <div style={{ marginTop: 20 }}>
        <HistoryTable />
      </div>
      <Stats />
    </AppLayout>
  );
};

export default MonitorPage;
