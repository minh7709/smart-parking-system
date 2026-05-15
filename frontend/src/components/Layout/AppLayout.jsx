import React, { useState, useEffect } from "react";
import {
  Layout,
  Menu,
  Input,
  Badge,
  Avatar,
  ConfigProvider,
  Dropdown,
  message,
} from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  StarOutlined,
  VideoCameraOutlined,
  SearchOutlined,
  BellOutlined,
  UserOutlined,
  InfoCircleOutlined,
  LockOutlined,
  LogoutOutlined,
  CarOutlined,
} from "@ant-design/icons";
import { Outlet } from "react-router-dom";
import { useNotification } from "../../hooks/useNotification";

const { Header, Sider, Content } = Layout;

const styles = {
  sider: { background: "#ffffff", borderRight: "1px solid #f0f0f0" },
  logo: { color: "#141414", padding: 20, fontSize: 18, fontWeight: 600 },
  header: {
    background: "rgba(255,255,255,0.7)",
    backdropFilter: "blur(10px)",
    borderBottom: "1px solid #f0f0f0",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "0 24px",
  },
  search: { width: 250, background: "#ffffff", border: "1px solid #d9d9d9", color: "#141414" },
  userInfo: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    cursor: "pointer",
  },
  userName: {
    color: "#141414",
    fontSize: 14,
    maxWidth: 120,
    overflow: "hidden",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
};

export const AppLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const notify = useNotification();
  const [userName, setUserName] = useState("Người dùng");

  // Lấy thông tin user từ localStorage khi component mount
  useEffect(() => {
    const userInfo = localStorage.getItem("user");
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo);
        setUserName(
          user.fullName || user.username || user.email || "Người dùng",
        );
      } catch (e) {
        console.error("Lỗi parse userInfo:", e);
      }
    }
  }, []);

  // Xác định menu nào đang được chọn dựa trên URL hiện tại
  const selectedKey = location.pathname.includes("/register") ? "1" : location.pathname.includes("/vehicles") ? "3" : "2";

  // Hàm xử lý khi click vào Menu
  const handleMenuClick = (e) => {
    if (e.key === "1") navigate("/register");
    if (e.key === "3") navigate("/vehicles");
    if (e.key === "2") navigate("/monitor");
  };

  // Xử lý đăng xuất
  const handleLogout = () => {
    // Xóa tất cả dữ liệu trong localStorage
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("expiresIn");
    localStorage.removeItem("expiresAt");
    localStorage.removeItem("user");
    localStorage.removeItem("selectedCheckInLane");
    localStorage.removeItem("selectedCheckOutLane");

    notify.success("Đăng xuất thành công!");
    navigate("/login");
  };

  // Menu items cho dropdown
  const menuItems = [
    {
      key: "profile",
      icon: <InfoCircleOutlined />,
      label: "Thông tin",
      onClick: () => navigate("/profile"),
    },
    {
      key: "change-password",
      icon: <LockOutlined />,
      label: "Đổi mật khẩu",
      onClick: () => navigate("/change-password"),
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
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#1677ff",
          colorBgBase: "#ffffff",
          colorTextBase: "#141414",
          borderRadius: 14,
        },
        components: {
          Dropdown: {
            colorBgElevated: "#ffffff",
            controlItemBgHover: "#f5f5f5",
          },
        },
      }}
    >
      <Layout style={{ minHeight: "100vh", background: "#f5f5f5" }}>
        {/* SIDEBAR */}
        <Sider width={230} style={styles.sider}>
          <div style={styles.logo}>Smart Parking</div>
          <Menu
            theme="light"
            mode="inline"
            selectedKeys={[selectedKey]}
            onClick={handleMenuClick}
            style={{ background: "transparent", borderRight: 0 }}
            items={[
              { key: "1", icon: <StarOutlined />, label: "Vé tháng" },
              { key: "3", icon: <CarOutlined />, label: "Phương tiện" },
              { key: "2", icon: <VideoCameraOutlined />, label: "Camera" },
            ]}
          />
        </Sider>

        {/* MAIN CONTENT */}
        <Layout style={{ background: "transparent" }}>
          {/* TOPBAR */}
          <Header style={styles.header}>
            <h2 style={{ color: "#141414", margin: 0 }}>{selectedKey === "1" ? "Vé tháng" : selectedKey === "3" ? "Phương tiện" : "Camera"}</h2>
            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
              <Badge dot>
                <BellOutlined style={{ fontSize: 18, color: "#555" }} />
              </Badge>

              {/* Dropdown Avatar với thông tin user */}
              <Dropdown menu={{ items: menuItems }} trigger={["click"]}>
                <div style={styles.userInfo}>
                  <Avatar icon={<UserOutlined />} />
                  <span style={styles.userName}>{userName}</span>
                </div>
              </Dropdown>
            </div>
          </Header>
          <Content style={{ margin: 20 }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  );
};

