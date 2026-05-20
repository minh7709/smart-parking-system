import { Routes, Route, Navigate } from "react-router-dom";
// Import cÃ¡c Layout
import { AppLayout } from "../components/Layout/AppLayout";
import { AdminLayout } from "../components/Layout/AdminLayout";

// Import cÃ¡c trang
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
import AdminRegisterPage from "../features/admin/pages/AdminRegisterPage";
import TurnTicketConfig from "../features/admin/pages/TurnTicketConfig";
import UserManagementPage from "../features/admin/pages/UserManagementPage";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />

      <Route path="/lane" element={<LanePage />} />

      <Route element={<AppLayout />}>
        <Route path="/monitor" element={<MonitorPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/vehicles" element={<VehiclePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/change-password" element={<ChangePasswordPage />} />
      </Route>

      {/* NHÃ“M ROUTE CHO ADMIN */}
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
        <Route path="/admin/users" element={<UserManagementPage />} />
        <Route path="/admin/incidents" element={<AdminIncident />} />
        <Route path="/admin/lanes" element={<AdminLane />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default AppRoutes;

