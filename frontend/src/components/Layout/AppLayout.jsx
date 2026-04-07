import React from "react";
import { Layout, Menu, Input, Badge, Avatar, ConfigProvider } from "antd";
import {
  DashboardOutlined,
  VideoCameraOutlined,
  SearchOutlined,
  BellOutlined,
  UserOutlined,
} from "@ant-design/icons";

const { Header, Sider, Content } = Layout;

const styles = {
  sider: { background: "#070707", borderRight: "1px solid #1a1a1a" },
  logo: { color: "#fff", padding: 20, fontSize: 18, fontWeight: 600 },
  header: {
    background: "rgba(20,20,20,0.7)",
    backdropFilter: "blur(10px)",
    borderBottom: "1px solid #1f1f1f",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    padding: "0 24px",
  },
  search: { width: 250, background: "#1a1a1a", border: "none", color: "#fff" },
};

export const AppLayout = ({ children }) => {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#404241",
          colorBgBase: "#0b0b0b",
          colorTextBase: "#ffffff",
          borderRadius: 14,
        },
      }}
    >
      <Layout style={{ minHeight: "100vh", background: "#0b0b0b" }}>
        {/* SIDEBAR */}
        <Sider width={230} style={styles.sider}>
          <div style={styles.logo}>Smart Parking</div>
          <Menu
            theme="dark"
            mode="inline"
            defaultSelectedKeys={["2"]}
            style={{ background: "transparent" }}
            items={[
              { key: "1", icon: <DashboardOutlined />, label: "Dashboard" },
              { key: "2", icon: <VideoCameraOutlined />, label: "Camera" },
            ]}
          />
        </Sider>

        {/* MAIN CONTENT */}
        <Layout style={{ background: "transparent" }}>
          {/* TOPBAR */}
          <Header style={styles.header}>
            <h2 style={{ color: "#fff", margin: 0 }}>Camera</h2>
            <div style={{ display: "flex", gap: 16, alignItems: "center" }}>
              <Input
                placeholder="Search..."
                prefix={<SearchOutlined />}
                style={styles.search}
              />
              <Badge dot>
                <BellOutlined style={{ fontSize: 18, color: "#aaa" }} />
              </Badge>
              <Avatar icon={<UserOutlined />} />
            </div>
          </Header>

          {/* NỘI DUNG TỪNG TRANG */}
          <Content style={{ margin: 20 }}>{children}</Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  );
};
