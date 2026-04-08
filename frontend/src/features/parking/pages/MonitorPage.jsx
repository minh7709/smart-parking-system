import React from "react";
import { Row, Col } from "antd";
import { AppLayout } from "../../../components/Layout/AppLayout";
import CameraCard from "../components/CameraCard";
import HistoryTable from "../components/HistoryTable";
import Stats from "../components/Stats";

const CAMERA_IN_URL = "http://192.168.8.88:4747/video";
const CAMERA_OUT_URL = "http://192.168.61.20:4747/video";

const MonitorPage = () => {
  return (
    <AppLayout>
      {/* 2 CAMERA IN/OUT */}
      <Row gutter={24} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <CameraCard
            type="IN"
            title="LÀN VÀO"
            plateNumber="ABC-8842"
            imgSrc={CAMERA_IN_URL}
          />
        </Col>
        <Col span={12}>
          <CameraCard
            type="OUT"
            title="LÀN RA"
            plateNumber="XYZ-1092"
            imgSrc={CAMERA_OUT_URL}
          />
        </Col>
      </Row>

      {/* BẢNG LỊCH SỬ */}
      <div style={{ marginTop: 20 }}>
        <HistoryTable />
      </div>

      {/* KHỐI THỐNG KÊ */}
      <Stats />
    </AppLayout>
  );
};

export default MonitorPage;
