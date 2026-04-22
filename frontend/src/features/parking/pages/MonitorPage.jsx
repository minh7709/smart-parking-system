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

  const cameraInUrl = checkInLane?.ipCamera
    ? `http://${checkInLane.ipCamera}:4747/video`
    : null;
  const cameraOutUrl = checkOutLane?.ipCamera
    ? `http://${checkOutLane.ipCamera}:4747/video`
    : null;
  // const cameraInUrl = "http://10.251.11.26:4747/video";

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
    <>
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
            videoSrc={cameraInUrl}
            onSuccess={(data) => console.log("Check-in success", data)}
          />
        </Col>
        <Col span={12}>
          <CameraCard
            type="OUT"
            title={checkOutLane?.laneName || "LÀN RA"}
            laneId={checkOutLane?.id}
            videoSrc={cameraInUrl} // tạm thời dùng chung, sau này thay bằng camera out riêng
            onSuccess={(data) => console.log("Check-out success", data)}
          />
        </Col>
      </Row>
      <div style={{ marginTop: 20 }}>
        <HistoryTable />
      </div>
      <Stats />
    </>
  );
};

export default MonitorPage;
