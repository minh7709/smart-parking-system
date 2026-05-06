import { Routes, Route, Navigate } from "react-router-dom";
// Import các Layout
import { AppLayout } from "../components/Layout/AppLayout";
import { AdminLayout } from "../components/Layout/AdminLayout";

// Import các trang
import LoginPage from "../features/auth/pages/LoginPage";
import Dashboard from "../features/parking/pages/Dashboard";
import MonitorPage from "../features/parking/pages/MonitorPage";
import RegisterPage from "../features/subscription/pages/RegisterPage";
import LanePage from "../features/parking/pages/LanePage";
import ProfilePage from "../features/parking/pages/ProfilePage";
import ChangePasswordPage from "../features/parking/pages/ChangePasswordPage";
import AdminDashboard from "../features/admin/pages/AdminDashboard";
import TurnTicketConfig from "../features/admin/pages/TurnTicketConfig";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />

      {/* NHÓM ROUTE CHO BẢO VỆ */}
      <Route element={<AppLayout />}>
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/lane" element={<LanePage />} />
        <Route path="/monitor" element={<MonitorPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/change-password" element={<ChangePasswordPage />} />
      </Route>

      {/* NHÓM ROUTE CHO ADMIN */}
      <Route element={<AdminLayout />}>
        <Route
          path="/admin"
          element={<Navigate to="/admin/dashboard" replace />}
        />
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/profile" element={<ProfilePage />} />
        <Route path="/admin/change-password" element={<ChangePasswordPage />} />
        <Route path="/admin/turn-tickets" element={<TurnTicketConfig />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;
