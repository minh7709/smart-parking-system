import React, { useState, useEffect } from "react";
import { Layout, Menu, Input, Avatar, Badge, Dropdown, message } from "antd";
import {
  BarChartOutlined,
  IdcardOutlined,
  UserOutlined,
  TeamOutlined,
  CarOutlined,
  LogoutOutlined,
  BellOutlined,
  SettingOutlined,
  SearchOutlined,
  InfoCircleOutlined,
  LockOutlined,
} from "@ant-design/icons";
import { Outlet, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;

export const AdminLayout = () => {
  const navigate = useNavigate();
  const [userName, setUserName] = useState("Admin");

  // Lấy thông tin admin từ localStorage
  useEffect(() => {
    const userInfo = localStorage.getItem("user");
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo);
        setUserName(user.fullName || user.username || "Admin");
      } catch (e) {
        console.error("Lỗi parse userInfo:", e);
      }
    }
  }, []);

  const handleLogout = () => {
    localStorage.clear();
    message.success("Đăng xuất thành công!");
    navigate("/login");
  };

  // Menu cho Dropdown của Admin
  const adminMenuItems = [
    {
      key: "profile",
      icon: <InfoCircleOutlined />,
      label: "Thông tin",
      onClick: () => navigate("/admin/profile"), // Điều hướng về đúng route admin profile
    },
    {
      key: "change-password",
      icon: <LockOutlined />,
      label: "Đổi mật khẩu",
      onClick: () => navigate("/admin/change-password"),
    },
    {
      type: "divider",
    },
    {
      key: "logout",
      icon: <LogoutOutlined />,
      label: "Đăng xuất",
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <Layout style={{ minHeight: "100vh" }}>
      {/* SIDEBAR */}
      <Sider
        width={250}
        theme="light"
        style={{ borderRight: "1px solid #f0f0f0" }}
      >
        <div
          style={{
            padding: "20px",
            fontSize: "20px",
            fontWeight: "bold",
            color: "#1890ff",
          }}
        >
          Admin Panel
        </div>
        <Menu
          mode="inline"
          defaultSelectedKeys={["1"]}
          style={{ borderRight: 0 }}
          onClick={(e) => {
            if (e.key === "1") navigate("/admin/dashboard");
            if (e.key === "2") navigate("/admin/turn-tickets");
          }}
          items={[
            {
              key: "1",
              icon: <BarChartOutlined />,
              label: "Thống kê doanh thu",
            },
            { key: "2", icon: <IdcardOutlined />, label: "Cấu hình vé" },
            { key: "3", icon: <UserOutlined />, label: "Danh sách khách hàng" },
            { key: "4", icon: <TeamOutlined />, label: "Quản lý nhân sự" },
            { key: "5", icon: <CarOutlined />, label: "Quản lý xe" },
          ]}
        />
      </Sider>

      <Layout>
        {/* HEADER */}
        <Header
          style={{
            background: "#fff",
            padding: "0 24px",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            borderBottom: "1px solid #f0f0f0",
          }}
        >
          <h2 style={{ margin: 0, fontSize: "18px" }}>Thống kê</h2>
          <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
            <Input
              placeholder="Tìm kiếm..."
              prefix={<SearchOutlined />}
              style={{
                width: 250,
                borderRadius: "20px",
                backgroundColor: "#f5f5f5",
                border: "none",
              }}
            />
            <Badge count={5}>
              <BellOutlined style={{ fontSize: "20px", cursor: "pointer" }} />
            </Badge>

            {/* Thêm Dropdown bao bọc Avatar và Tên ở đây */}
            <Dropdown menu={{ items: adminMenuItems }} trigger={["click"]}>
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "10px",
                  cursor: "pointer",
                }}
              >
                <Avatar
                  style={{ backgroundColor: "#87d068" }}
                  icon={<UserOutlined />}
                />
                <span style={{ fontWeight: 500, color: "#333" }}>
                  {userName}
                </span>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content style={{ padding: "24px", backgroundColor: "#f9fafc" }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};
