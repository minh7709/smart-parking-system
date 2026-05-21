import { Routes, Route, Navigate } from "react-router-dom";
import { Result, Button } from "antd";

import { AppLayout } from "../components/Layout/AppLayout";
import { AdminLayout } from "../components/Layout/AdminLayout";
import ProtectedRoute from "./ProtectedRoute";

import LoginPage from "../features/auth/pages/LoginPage";
import MonitorPage from "../features/parking/pages/MonitorPage";
import RegisterPage from "../features/parking/pages/RegisterPage";
import VehiclePage from "../features/parking/pages/VehiclePage";
import LanePage from "../features/parking/pages/LanePage";
import ProfilePage from "../features/auth/pages/ProfilePage";
import ChangePasswordPage from "../features/auth/pages/ChangePasswordPage";
import AdminDashboard from "../features/admin/pages/AdminDashboard";
import AdminIncident from "../features/admin/pages/AdminIncident";
import AdminLane from "../features/admin/pages/AdminLane";
import AdminMonthTicket from "../features/admin/pages/AdminMonthTicket";
import AdminRegisterPage from "../features/admin/pages/AdminRegisterPage";
import TurnTicketConfig from "../features/admin/pages/TurnTicketConfig";
import UserManagementPage from "../features/admin/pages/UserManagementPage";

const ForbiddenPage = () => (
  <Result
    status="403"
    title="403"
    subTitle="Xin lỗi, bạn không có quyền truy cập vào chức năng này."
    extra={
      <Button type="primary" onClick={() => window.location.href = localStorage.getItem("user").role === "ADMIN" ? "/admin" : "/monitor"}>
        Quay lại trang chủ
      </Button>
    }
    style={{ marginTop: "50px" }}
  />
);

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/403" element={<ForbiddenPage />} />

      <Route element={<ProtectedRoute allowedRoles={["GUARD", "ADMIN"]} />}>
        <Route element={<AppLayout />}>
          <Route path="/monitor" element={<MonitorPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/vehicles" element={<VehiclePage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/change-password" element={<ChangePasswordPage />} />
        </Route>
        <Route path="/lane" element={<LanePage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
        <Route element={<AdminLayout />}>
          <Route
            path="/admin"
            element={<Navigate to="/admin/dashboard" replace />}
          />
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/profile" element={<ProfilePage />} />
          <Route path="/admin/change-password" element={<ChangePasswordPage />} />
          <Route path="/admin/turn-tickets" element={<TurnTicketConfig />} />
          <Route path="/admin/register" element={<AdminRegisterPage />} />
          <Route path="/admin/month-tickets" element={<AdminMonthTicket />} />
          <Route path="/admin/users" element={<UserManagementPage />} />
          <Route path="/admin/incidents" element={<AdminIncident />} />
          <Route path="/admin/lanes" element={<AdminLane />} />
        </Route>
      </Route>

      {/* Điều hướng các URL không tồn tại quay lại màn hình đăng nhập */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;