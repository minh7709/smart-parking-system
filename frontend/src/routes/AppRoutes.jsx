import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../features/auth/pages/LoginPage";
import Dashboard from "../features/parking/pages/Dashboard";
import MonitorPage from "../features/parking/pages/MonitorPage";
import RegisterPage from "../features/subscription/pages/RegisterPage";
import LanePage from "../features/parking/pages/LanePage";
import ProfilePage from "../features/parking/pages/ProfilePage";
import ChangePasswordPage from "../features/parking/pages/ChangePasswordPage";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/lane" element={<LanePage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
      <Route path="/monitor" element={<MonitorPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/profile" element={<ProfilePage />} />
      <Route path="/change-password" element={<ChangePasswordPage />} />
    </Routes>
  );
};

export default AppRoutes;
