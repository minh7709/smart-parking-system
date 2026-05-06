import { useNavigate } from 'react-router-dom';
import { clearAuthFromLocalStorage, clearLaneSelection } from '../utils/storage';

const useLogout = () => {
  const navigate = useNavigate();

  return (redirectPath = '/login') => {
    clearAuthFromLocalStorage();
    clearLaneSelection();
    localStorage.removeItem('resetPasswordToken');

    navigate(redirectPath, { replace: true });
  };
};

export default useLogout;