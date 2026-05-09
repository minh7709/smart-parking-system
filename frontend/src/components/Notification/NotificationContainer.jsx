import React from 'react';
import { notification as antNotification } from 'antd';
import { useNotificationContext } from '../../context/NotificationContext';

/**
 * Component này có nhiệm vụ "lắng nghe" sự thay đổi của danh sách notifications
 * trong Context. Mỗi khi có một notification mới được thêm vào, nó sẽ sử dụng
 * thư viện Ant Design để hiển thị thông báo đó lên màn hình.
 * 
 * Nó không render ra bất kỳ HTML nào, chỉ dùng để kích hoạt hiệu ứng (side-effect).
 */
export const NotificationContainer = () => {
  const { notifications, closeNotification } = useNotificationContext();
  const [api, contextHolder] = antNotification.useNotification();

  // Sử dụng Set để theo dõi các ID đã được hiển thị, tránh lặp lại
  const displayedNotifications = React.useRef(new Set());

  React.useEffect(() => {
    notifications.forEach(notif => {
      // Chỉ hiển thị nếu notification này chưa từng được hiển thị trước đó
      if (!displayedNotifications.current.has(notif.id)) {
        api[notif.type]({
          key: notif.id,
          message: notif.title,
          description: notif.message,
          duration: notif.duration,
          onClose: () => {
            closeNotification(notif.id);
            displayedNotifications.current.delete(notif.id);
          },
        });
        // Đánh dấu là đã hiển thị
        displayedNotifications.current.add(notif.id);
      }
    });
  }, [notifications, api, closeNotification]);

  // contextHolder là một phần bắt buộc của Ant Design để nó biết vị trí render thông báo.
  return <>{contextHolder}</>;
};
