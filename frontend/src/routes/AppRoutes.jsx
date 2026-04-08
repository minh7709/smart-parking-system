import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../features/auth/pages/LoginPage";
import Dashboard from "../features/parking/pages/Dashboard";
import MonitorPage from "../features/parking/pages/MonitorPage";
import Lanepage from "../features/parking/pages/Lanepage";

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/lane" element={<Lanepage/>} />
      <Route path="*" element={<Navigate to="/login" replace />} />
      <Route path="/monitor" element={<MonitorPage />} />
    </Routes>
  );
};

export default AppRoutes;
