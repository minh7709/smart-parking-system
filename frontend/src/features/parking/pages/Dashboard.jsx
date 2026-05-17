import React from "react";
import { useNavigate } from "react-router-dom";
import styles from "./Dashboard.module.css";

const Dashboard = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Clear auth data
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("tokenType");
    localStorage.removeItem("expiresIn");
    localStorage.removeItem("expiresAt");
    localStorage.removeItem("user");

    navigate("/login");
  };

  const user = JSON.parse(localStorage.getItem("user") || "{}");

  return (
    <div className={styles['dashboard-container']}>
      <nav className={styles.navbar}>
        <div className={styles['navbar-brand']}>Smart Parking System</div>
        <div className={styles['navbar-user']}>
          <span>Welcome, {user.fullName || "User"}</span>
          <button onClick={handleLogout} className={styles['logout-btn']}>
            Logout
          </button>
        </div>
      </nav>

      <div className={styles['dashboard-content']}>
        <h1>Dashboard</h1>
        <div className={styles['user-info']}>
          <h2>Your Information</h2>
          <p>
            <strong>Username:</strong> {user.username}
          </p>
          <p>
            <strong>Full Name:</strong> {user.fullName}
          </p>
          <p>
            <strong>Role:</strong> {user.role}
          </p>
          <p>
            <strong>Status:</strong> {user.status}
          </p>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
