import { useNotificationContext } from '../context/NotificationContext';

/**
 * Custom hook để cung cấp các hàm tiện ích cho việc hiển thị thông báo.
 * Giúp code ở các component khác ngắn gọn hơn.
 * Ví dụ: thay vì gọi showNotification({ type: 'success', ... }), chỉ cần gọi notify.success('Message').
 *
 * notify.apiError(err) — dùng cho lỗi từ axiosClient:
 *   - message  ← err.payload.message  (hoặc err.message nếu không có payload)
 *   - title    ← err.payload.errorCode (hoặc 'Đã có lỗi xảy ra')
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

    /** Hiển thị lỗi từ backend (axiosClient reject).
     *  Tự động lấy err.payload.message và err.payload.errorCode */
    apiError: (err, fallbackMessage = 'Đã có lỗi xảy ra') => {
      const message = err?.payload?.fieldErrors?.[0]?.message || err?.payload?.message || err?.message || fallbackMessage;
      const title = err?.payload?.errorCode || 'Đã có lỗi xảy ra';
      showNotification({ type: 'error', title, message });
    },
  };
};
