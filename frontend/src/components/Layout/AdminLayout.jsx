import React, { useMemo, useState, useEffect } from "react";
import { Layout, Menu, Avatar, Dropdown } from "antd";
import {
  BarChartOutlined,
  IdcardOutlined,
  UserOutlined,
  TeamOutlined,
  CarOutlined,
  AlertOutlined,
  ApartmentOutlined,
  LogoutOutlined,
  InfoCircleOutlined,
  LockOutlined,
} from "@ant-design/icons";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { useNotification } from "../../hooks/useNotification";

const { Header, Sider, Content } = Layout;

export const AdminLayout = () => {
  const navigate = useNavigate();
  const location = useLocation(); // Hook lắng nghe sự thay đổi của URL
  const notify = useNotification();
  const [userName, setUserName] = useState("Admin");

  // Đồng bộ hóa trạng thái sáng của Menu dựa trên URL hiện tại
  const selectedKey = useMemo(() => location.pathname, [location.pathname]);

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
    notify.success("Đăng xuất thành công!");
    navigate("/login");
  };

  // Cấu hình các mục Menu cho Sidebar - Đặt key trùng khớp với Route của dự án luôn
  const sidebarMenuItems = [
    {
      key: "/admin/dashboard",
      icon: <BarChartOutlined />,
      label: "Thống kê doanh thu",
    },
    { 
      key: "/admin/turn-tickets", 
      icon: <IdcardOutlined />, 
      label: "Cấu hình giá vé lượt" 
    },
    { 
      key: "/admin/register", 
      icon: <CarOutlined />, 
      label: "Quản lý gói đăng ký" 
    },
    { 
      key: "/admin/month-tickets", 
      icon: <IdcardOutlined />, 
      label: "Cấu hình giá vé đăng ký" 
    },
    { 
      key: "/admin/users", 
      icon: <TeamOutlined />, 
      label: "Quản lý nhân sự" 
    },
    { 
      key: "/admin/incidents", 
      icon: <AlertOutlined />, 
      label: "Báo cáo sự cố" 
    },
    { 
      key: "/admin/lanes", 
      icon: <ApartmentOutlined />, 
      label: "Quản lý làn" 
    },
  ];

  // Menu cho Dropdown của Admin (Góc trên cùng bên phải)
  const adminMenuItems = [
    {
      key: "profile",
      icon: <InfoCircleOutlined />,
      label: "Thông tin",
      onClick: () => navigate("/admin/profile"),
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
          onClick={() => navigate("/admin/dashboard")}
          style={{
            padding: "20px",
            fontSize: "20px",
            fontWeight: "bold",
            color: "#1890ff",
            cursor: "pointer",
          }}
        >
          Admin Panel
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]} // Chuyển từ defaultSelectedKeys sang selectedKeys cố định theo State
          style={{ borderRight: 0 }}
          onClick={(e) => {
            // e.key lúc này chính là path của Route (Ví dụ: "/admin/dashboard")
            navigate(e.key);
          }}
          items={sidebarMenuItems}
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
          <h2 style={{ margin: 0, fontSize: "18px" }}>Quản trị</h2>
          <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
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

        {/* CONTENT */}
        <Content style={{ padding: "24px", backgroundColor: "#f9fafc" }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};