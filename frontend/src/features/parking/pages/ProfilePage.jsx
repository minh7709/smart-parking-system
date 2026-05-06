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
  const [user, setUser] = useState(null);

  useEffect(() => {
    const userData = localStorage.getItem("user");
    if (userData) {
      setUser(JSON.parse(userData));
    } else {
      message.error("Không tìm thấy thông tin người dùng!");
      navigate("/login");
    }
  }, [navigate]);

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
        background: "#141414",
        border: "1px solid #1f1f1f",
        borderRadius: 16,
      }}
      headStyle={{ borderBottom: "1px solid #1f1f1f", color: "#fff" }}
    >
      <div style={{ textAlign: "center", marginBottom: 24 }}>
        <Avatar
          size={80}
          icon={<UserOutlined />}
          style={{ backgroundColor: "#1890ff" }}
        />
      </div>

      <Descriptions
        bordered
        column={1}
        labelStyle={{ color: "#aaa", background: "#1a1a1a" }}
        contentStyle={{ color: "#fff", background: "#141414" }}
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
          {user.role || "USER"}
        </Descriptions.Item>
        <Descriptions.Item label="Trạng thái">
          {user.status || "ACTIVE"}
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
