import { useNavigate } from 'react-router-dom';
import { clearAuthFromLocalStorage, clearLaneSelection, clearSystemTypes } from '../utils/storage';
import { useNotification } from './useNotification';

const useLogout = () => {
  const navigate = useNavigate();
  const notify = useNotification();
  return (redirectPath = '/login') => {
    clearAuthFromLocalStorage();
    clearLaneSelection();
    localStorage.removeItem('resetPasswordToken');
    clearSystemTypes();
    notify.success("Đã đăng xuất thành công!");
    navigate(redirectPath, { replace: true });
  };
};

export default useLogout;