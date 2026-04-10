import React from "react";
import { Card, Button, Space, Tooltip } from "antd";
import {
  ZoomInOutlined,
  VideoCameraOutlined,
  SettingOutlined,
  ExpandOutlined,
} from "@ant-design/icons";

const CameraCard = ({ title, type, plateNumber, imgSrc }) => {
  // Lấy màu chủ đạo tùy theo camera (IN: Xanh lá, OUT: Xanh dương)
  const themeColor = type === "IN" ? "#ffffff" : "#ffffff";

  return (
    <Card
      title={
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <span
            style={{
              width: 8,
              height: 8,
              borderRadius: "50%",
              background: themeColor,
              boxShadow: `0 0 8px ${themeColor}`,
            }}
          ></span>
          <span style={{ color: "#fff", fontWeight: 600, letterSpacing: 1 }}>
            {title}
          </span>
        </div>
      }
      // Khung chứa các nút thao tác camera
      extra={
        <Space size="small">
          <Tooltip title="Phóng to">
            <Button
              type="text"
              shape="circle"
              icon={<ZoomInOutlined style={{ color: "#ffffff" }} />}
            />
          </Tooltip>
          <Tooltip title="Cài đặt luồng">
            <Button
              type="text"
              shape="circle"
              icon={<SettingOutlined style={{ color: "#ffffff" }} />}
            />
          </Tooltip>
          <Tooltip title="Mở toàn màn hình">
            <Button
              type="text"
              shape="circle"
              icon={<ExpandOutlined style={{ color: "#ffffff" }} />}
            />
          </Tooltip>
        </Space>
      }
      bordered={false}
      style={{
        background: "#141414",
        border: "1px solid #1f1f1f",
        borderRadius: 16,
        overflow: "hidden",
      }}
      styles={{
        header: { borderBottom: "1px solid #1f1f1f", padding: "12px 16px" },
        body: { padding: 12 },
      }}
    >
      <div
        style={{
          height: 260,
          borderRadius: 8,
          overflow: "hidden",
          position: "relative",
          background: "#000",
        }}
      >
        {/* Hình ảnh luồng Camera */}
        <img
          src={imgSrc}
          alt="camera-feed"
          style={{ width: "100%", height: "100%", objectFit: "cover" }}
        />

        {/* Lớp phủ đen từ dưới lên để làm nổi bật biển số */}
        <div
          style={{
            position: "absolute",
            inset: 0,
            background:
              "linear-gradient(to top, rgba(0,0,0,0.9) 0%, transparent 40%)",
          }}
        />

        {/* Trạng thái LIVE góc trái */}
        <div
          style={{
            position: "absolute",
            top: 12,
            left: 12,
            background: "rgba(0,0,0,0.6)",
            backdropFilter: "blur(4px)",
            padding: "4px 10px",
            borderRadius: 4,
            border: "1px solid rgba(255,255,255,0.1)",
          }}
        >
          <span
            style={{
              color: "#fff",
              fontSize: 10,
              fontWeight: 700,
              letterSpacing: 1,
            }}
          >
            LIVE • 30FPS
          </span>
        </div>

        {/* Khung hiển thị biển số nổi lên trên Camera */}
        {plateNumber && (
          <div
            style={{
              position: "absolute",
              bottom: 5,
              left: "50%",
              transform: "translateX(-50%)",
              background: "rgba(0,0,0,0.7)",
              backdropFilter: "blur(8px)",
              padding: "6px 32px",
              borderRadius: 8,
              border: `1px solid ${themeColor}`,
              boxShadow: `0 4px 20px rgba(0,0,0,0.5)`,
            }}
          >
            <span
              style={{
                color: themeColor,
                fontSize: 15,
                fontWeight: 900,
                letterSpacing: 2,
                fontFamily: "monospace",
              }}
            >
              {plateNumber}
            </span>
          </div>
        )}
      </div>
    </Card>
  );
};

export default CameraCard;
