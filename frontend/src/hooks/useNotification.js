import { useNotificationContext } from '../context/NotificationContext';

/**
 * Custom hook để cung cấp các hàm tiện ích cho việc hiển thị thông báo.
 * Giúp code ở các component khác ngắn gọn hơn.
 * Ví dụ: thay vì gọi showNotification({ type: 'success', ... }), chỉ cần gọi notify.success('Message').
 */
export const useNotification = () => {
  const { showNotification } = useNotificationContext();

  if (showNotification === undefined) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }

  return {
    success: (message, title = 'Thành công') => 
      showNotification({ type: 'success', title, message }),
    
    error: (message, title = 'Đã có lỗi xảy ra') => 
      showNotification({ type: 'error', title, message }),
      
    warning: (message, title = 'Cảnh báo') => 
      showNotification({ type: 'warning', title, message }),
      
    info: (message, title = 'Thông tin') => 
      showNotification({ type: 'info', title, message }),
  };
};
