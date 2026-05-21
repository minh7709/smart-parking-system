import React, { useState, useEffect } from "react";
import { Card, Descriptions, Avatar, Button, message } from "antd";
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  IdcardOutlined,
} from "@ant-design/icons";
import { AppLayout } from "../../../components/Layout/AppLayout";
import { useNavigate } from "react-router-dom";

const ProfilePage = () => {
  const navigate = useNavigate();
  const [user] = useState(() => {
    const userData = localStorage.getItem("user");
    return userData ? JSON.parse(userData) : null;
  });

  useEffect(() => {
    if (!user) {
      message.error("Không tìm thấy thông tin người dùng!");
      navigate("/login");
    }
  }, [user, navigate]);

  if (!user) {
    return (
      <AppLayout>
        <div style={{ textAlign: "center", padding: 50 }}>Đang tải...</div>
      </AppLayout>
    );
  }

  return (
    <Card
      title="Thông tin cá nhân"
      style={{
        maxWidth: 800,
        margin: "0 auto",
        background: "#ffffff",
        border: "1px solid #f0f0f0",
        borderRadius: 16,
        boxShadow: "0 8px 24px rgba(0, 0, 0, 0.04)",
      }}
      headStyle={{ borderBottom: "1px solid #f0f0f0", color: "#141414" }}
    >
      <div style={{ textAlign: "center", marginBottom: 24 }}>
        <Avatar
          size={80}
          icon={<UserOutlined />}
          style={{ backgroundColor: "#1677ff" }}
        />
      </div>

      <Descriptions
        bordered
        column={1}
        labelStyle={{ color: "#555", background: "#fafafa" }}
        contentStyle={{ color: "#141414", background: "#ffffff" }}
      >
        <Descriptions.Item
          label={
            <>
              <UserOutlined /> Họ và tên
            </>
          }
        >
          {user.fullName || "Chưa cập nhật"}
        </Descriptions.Item>
        <Descriptions.Item
          label={
            <>
              <IdcardOutlined /> Tên đăng nhập
            </>
          }
        >
          {user.username || "Chưa cập nhật"}
        </Descriptions.Item>
        <Descriptions.Item
          label={
            <>
              <MailOutlined /> Email
            </>
          }
        >
          {user.email || "Chưa cập nhật"}
        </Descriptions.Item>
        <Descriptions.Item
          label={
            <>
              <PhoneOutlined /> Số điện thoại
            </>
          }
        >
          {user.phone || "Chưa cập nhật"}
        </Descriptions.Item>
        <Descriptions.Item label="Vai trò">
          {user.role || "Người dùng"}
        </Descriptions.Item>
        <Descriptions.Item label="Trạng thái">
          {user.status || "Đang hoạt động"}
        </Descriptions.Item>
      </Descriptions>

      <div style={{ textAlign: "center", marginTop: 24 }}>
        <Button onClick={() => navigate("/change-password")} type="primary">
          Đổi mật khẩu
        </Button>
      </div>
    </Card>
  );
};

export default ProfilePage;
