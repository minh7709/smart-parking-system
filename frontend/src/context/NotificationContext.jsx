import React, { createContext, useState, useCallback, useContext } from 'react';

// 1. Định nghĩa "cái tủ" (Context)
export const NotificationContext = createContext(null);

// 2. Tạo "người quản lý tủ" (Provider)
export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([]);

  // Hàm để hiển thị thông báo
  const showNotification = useCallback((config) => {
    const id = Date.now() + Math.random(); // Tạo ID độc nhất
    const notification = {
      id,
      type: config.type || 'info', // 'success', 'error', 'warning', 'info'
      title: config.title,
      message: config.message,
      duration: config.duration === 0 ? 0 : config.duration || 3, // Mặc định 3s, 0 là không tự đóng
    };
    
    // Thêm thông báo mới vào danh sách
    setNotifications(prev => [...prev, notification]);
    
    // Tự động xóa sau một khoảng thời gian (nếu duration > 0)
    if (notification.duration > 0) {
      setTimeout(() => {
        closeNotification(notification.id);
      }, notification.duration * 1000);
    }
    
    return id;
  }, []);

  // Hàm để đóng một thông báo cụ thể
  const closeNotification = useCallback((id) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  }, []);

  // 3. "Giá trị" mà Provider cung cấp cho các component con
  const value = { 
    notifications, 
    showNotification, 
    closeNotification 
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};

// 4. Tạo một "lối tắt" (Custom Hook) để dễ dàng sử dụng
export const useNotificationContext = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotificationContext must be used within a NotificationProvider');
  }
  return context;
};
