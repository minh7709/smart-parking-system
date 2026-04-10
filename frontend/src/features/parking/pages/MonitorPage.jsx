import React from "react";
import { Row, Col } from "antd";
import { AppLayout } from "../../../components/Layout/AppLayout";
import CameraCard from "../components/CameraCard";
import HistoryTable from "../components/HistoryTable";
import Stats from "../components/Stats";

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
            imgSrc="https://i.pinimg.com/1200x/83/33/99/83339974e61603dc655517fd33e9fbb8.jpg"
          />
        </Col>
        <Col span={12}>
          <CameraCard
            type="OUT"
            title="LÀN RA"
            plateNumber="XYZ-1092"
            imgSrc="https://i.pinimg.com/1200x/7c/13/e1/7c13e112bec70032a710134ddc6cdc94.jpg"
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
