import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { forgotPasswordApi, loginApi, resetPasswordApi, verifyOtpApi } from '../api/auth.api';
import ForgotPasswordForm from '../components/ForgotPasswordForm';
import LoginForm from '../components/LoginForm';
import ResetPasswordForm from '../components/ResetPasswordForm';
import VerifyOtpForm from '../components/VerifyOtpForm';
import { saveAuthToLocalStorage } from '../../../utils/storage';
import { validateOtp, validatePassword, validatePhone } from '../../../utils/validators';
import './LoginPage.css';

const LoginPage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [resetToken, setResetToken] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [otpError, setOtpError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const showError = (message, field = 'general') => {
    if (field === 'general') setError(message);
    else if (field === 'phone') setPhoneError(message);
    else if (field === 'otp') setOtpError(message);
    else if (field === 'password') setPasswordError(message);
  };

  const clearErrors = () => {
    setError('');
    setPhoneError('');
    setOtpError('');
    setPasswordError('');
  };

  const resetForgotFlow = () => {
    setPhone('');
    setOtp('');
    setNewPassword('');
    setResetToken('');
    setStep('login');
    clearErrors();
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!username.trim() || !password.trim()) {
      showError('Vui long nhap username va password');
      return;
    }

    setLoading(true);

    try {
      const response = await loginApi({
        username,
        password,
        rememberMe,
      });

      if (response.success && response.data?.accessToken && response.data?.refreshToken) {
        saveAuthToLocalStorage(response.data);
        navigate('/dashboard');
      } else {
        showError(response.message || 'Du lieu dang nhap khong hop le.');
      }
    } catch (err) {
      console.error('Login error:', err);
      showError(err.message || 'Khong the ket noi toi server. Hay kiem tra backend.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!phone.trim()) {
      showError('Vui long nhap so dien thoai', 'phone');
      return;
    }

    if (!validatePhone(phone)) {
      showError('So dien thoai khong hop le (10-11 chu so)', 'phone');
      return;
    }

    setLoading(true);

    try {
      const response = await forgotPasswordApi({ phone });

      if (response.success) {
        setStep('otp');
        setError('');
      } else {
        showError(response.message || 'Khong the gui OTP.');
      }
    } catch (err) {
      console.error('Send OTP error:', err);
      showError(err.message || 'Khong the ket noi toi server.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!validateOtp(otp)) {
      showError('OTP phai gom dung 6 chu so.', 'otp');
      return;
    }

    setLoading(true);

    try {
      const response = await verifyOtpApi({ phone, otp });

      if (response.success && response.data) {
        setResetToken(response.data);
        setStep('reset');
        setError('');
      } else {
        showError(response.message || 'OTP khong hop le.', 'otp');
      }
    } catch (err) {
      console.error('Verify OTP error:', err);
      showError(err.message || 'Khong the xac minh OTP. Vui long thu lai.', 'otp');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!newPassword.trim()) {
      showError('Vui long nhap mat khau moi.', 'password');
      return;
    }

    if (!validatePassword(newPassword)) {
      showError('Mat khau phai co it nhat 8 ky tu, bao gom chu hoa, chu thuong va so.', 'password');
      return;
    }

    if (!resetToken) {
      showError('Token khong hop le. Vui long thu lai tu buoc xac minh OTP.');
      return;
    }

    setLoading(true);

    try {
      const response = await resetPasswordApi({
        newPassword,
        token: resetToken,
      });

      if (response.success) {
        setOtp('');
        setNewPassword('');
        setPhone('');
        setResetToken('');
        setStep('login');
        setError('');
      } else {
        showError(response.message || 'Khong the dat lai mat khau.');
      }
    } catch (err) {
      console.error('Reset password error:', err);
      showError(err.message || 'Khong the ket noi toi server.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="wrapper">
      {step === 'login' && (
        <LoginForm
          username={username}
          password={password}
          rememberMe={rememberMe}
          loading={loading}
          error={error}
          onUsernameChange={setUsername}
          onPasswordChange={setPassword}
          onRememberMeChange={setRememberMe}
          onSubmit={handleLogin}
          onForgotPassword={() => {
            setStep('forgot');
            clearErrors();
          }}
        />
      )}

      {step === 'forgot' && (
        <ForgotPasswordForm
          phone={phone}
          loading={loading}
          error={error}
          phoneError={phoneError}
          onPhoneChange={setPhone}
          onSubmit={handleSendOtp}
          onBack={resetForgotFlow}
        />
      )}

      {step === 'otp' && (
        <VerifyOtpForm
          otp={otp}
          loading={loading}
          error={error}
          otpError={otpError}
          onOtpChange={setOtp}
          onSubmit={handleVerifyOtp}
          onBack={() => setStep('forgot')}
        />
      )}

      {step === 'reset' && (
        <ResetPasswordForm
          newPassword={newPassword}
          loading={loading}
          error={error}
          passwordError={passwordError}
          onNewPasswordChange={setNewPassword}
          onSubmit={handleResetPassword}
          onBack={() => setStep('otp')}
        />
      )}
    </div>
  );
};

export default LoginPage;
