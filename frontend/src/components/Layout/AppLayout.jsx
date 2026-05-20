import React, { useState, useEffect } from "react";
import {
  Layout,
  Menu,
  Avatar,
  ConfigProvider,
  Dropdown,
  Button,
} from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import {
  StarOutlined,
  VideoCameraOutlined,
  UserOutlined,
  InfoCircleOutlined,
  LockOutlined,
  LogoutOutlined,
  CarOutlined,
  ArrowLeftOutlined,
} from "@ant-design/icons";
import { Outlet } from "react-router-dom";
import { useNotification } from "../../hooks/useNotification";

const { Header, Sider, Content } = Layout;

const styles = {
  sider: { background: "rgb(20, 66, 130)", borderRight: "1px solid #111827" },
  logo: { color: "#ffffff", padding: 20, fontSize: 18, fontWeight: 600, letterSpacing: 0.5 },
  header: {
    background: "#ffffff",
    borderBottom: "1px solid #e5e7eb",
    boxShadow: "0 6px 20px rgba(15, 23, 42, 0.08)",
    height: 64,
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "0 24px",
    position: "fixed",
    top: 0,
    left: 230,
    right: 0,
    width: "calc(100% - 230px)",
    zIndex: 1000,
  },
  search: { width: 250, background: "#ffffff", border: "1px solid #d9d9d9", color: "#141414" },
  userInfo: {
    display: "flex",
    alignItems: "center",
    gap: 12,
    cursor: "pointer",
  },
  userName: {
    color: "#111827",
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
          Menu: {
            darkItemBg: "#0b0b0b",
            darkItemSelectedBg: "#111827",
            darkItemHoverBg: "#111827",
            darkItemColor: "#e5e7eb",
            darkItemSelectedColor: "#ffffff",
            darkItemHoverColor: "#ffffff",
          },
        },
      }}
    >
      <Layout style={{ minHeight: "100vh", background: "#f3f4f6" }}>
        {/* SIDEBAR */}
        <Sider width={230} style={{ ...styles.sider, overflow: 'auto', height: '100vh', position: 'fixed', left: 0, top: 0, bottom: 0 }}>
          <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            <div>
              <div style={styles.logo}>Smart Parking</div>
              <Menu
                theme="dark"
                mode="inline"
                selectedKeys={[selectedKey]}
                onClick={handleMenuClick}
                style={{ background: "transparent", borderRight: 0, color: "#ffffff" }}
                items={[
                  { key: "1", icon: <StarOutlined />, label: "Vé tháng" },
                  { key: "3", icon: <CarOutlined />, label: "Phương tiện" },
                  { key: "2", icon: <VideoCameraOutlined />, label: "Camera" },
                ]}
              />
            </div>
            <div style={{ marginTop: 'auto', padding: '16px' }}>
              <Button
                type="default"
                block
                icon={<ArrowLeftOutlined />}
                style={{ background: "#111827", borderColor: "#1f2937", color: "#ffffff" }}
                onClick={() => navigate('/lane')}
              >
                Quay lại
              </Button>
            </div>
          </div>
        </Sider>

        {/* MAIN CONTENT */}
        <Layout style={{ marginLeft: 230, background: "transparent", minHeight: "100vh" }}>
          {/* TOPBAR */}
          <Header style={styles.header}>
            <h2 style={{ color: "#1d4ed8", margin: 0 }}>{selectedKey === "1" ? "Vé tháng" : selectedKey === "3" ? "Phương tiện" : "Camera"}</h2>
            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
              <Dropdown menu={{ items: menuItems }} trigger={["click"]}>
                <div style={styles.userInfo}>
                  <Avatar icon={<UserOutlined />} />
                  <span style={styles.userName}>{userName}</span>
                </div>
              </Dropdown>
            </div>
          </Header>
          <Content style={{ margin: "88px 24px 24px" }}>
            <Outlet />
          </Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  );
};

