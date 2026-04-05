import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// Validation functions
const validatePhone = (phone) => {
  const phoneRegex = /^\+?[0-9]{10,11}$/;
  return phoneRegex.test(phone);
};

const validatePassword = (password) => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
  return passwordRegex.test(password);
};

const validateOtp = (otp) => {
  return /^\d{6}$/.test(otp);
};

const Login = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [resetToken, setResetToken] = useState('');
  
  // Loading và error states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [otpError, setOtpError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  const saveAuthToLocalStorage = (authData) => {
    const expiresAt = Date.now() + authData.expiresIn * 1000;

    localStorage.setItem('accessToken', authData.accessToken);
    localStorage.setItem('refreshToken', authData.refreshToken);
    localStorage.setItem('tokenType', authData.tokenType);
    localStorage.setItem('expiresIn', String(authData.expiresIn));
    localStorage.setItem('expiresAt', String(expiresAt));
    localStorage.setItem('user', JSON.stringify(authData.user));
  };

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

  const handleLogin = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!username.trim() || !password.trim()) {
      showError('Vui lòng nhập username và password');
      return;
    }

    setLoading(true);

    const loginData = {
      username: username,
      password: password,
      rememberMe: rememberMe
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(loginData),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Đăng nhập thất bại.');
      }

      if (data.success && data.data?.accessToken && data.data?.refreshToken) {
        saveAuthToLocalStorage(data.data);
        // Redirect to dashboard
        navigate('/dashboard');
      } else {
        showError(data.message || 'Dữ liệu đăng nhập không hợp lệ.');
      }
    } catch (error) {
      console.error('Login error:', error);
      showError(error.message || 'Không thể kết nối tới server. Hãy kiểm tra backend.');
    } finally {
      setLoading(false);
    }
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!phone.trim()) {
      showError('Vui lòng nhập số điện thoại', 'phone');
      return;
    }

    if (!validatePhone(phone)) {
      showError('Số điện thoại không hợp lệ (10-11 chữ số)', 'phone');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/forgot-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phone: phone }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Gửi OTP thất bại.');
      }
      
      if (data.success) {
        setStep('otp');
        setError('');
      } else {
        showError(data.message || 'Không thể gửi OTP.');
      }
    } catch (error) {
      console.error('Send OTP error:', error);
      showError(error.message || 'Không thể kết nối tới server.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!validateOtp(otp)) {
      showError('OTP phải gồm đúng 6 chữ số.', 'otp');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/verify-otp`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ phone: phone, otp: otp }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Xác minh OTP thất bại.');
      }

      if (data.success && data.data) {
        // Save reset token for password reset step
        setResetToken(data.data);
        setStep('reset');
        setError('');
      } else {
        showError(data.message || 'OTP không hợp lệ.', 'otp');
      }
    } catch (error) {
      console.error('Verify OTP error:', error);
      showError(error.message || 'Không thể xác minh OTP. Vui lòng thử lại.', 'otp');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    clearErrors();

    if (!newPassword.trim()) {
      showError('Vui lòng nhập mật khẩu mới.', 'password');
      return;
    }

    if (!validatePassword(newPassword)) {
      showError('Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.', 'password');
      return;
    }

    if (!resetToken) {
      showError('Token không hợp lệ. Vui lòng thử lại từ bước xác minh OTP.');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/api/v1/auth/reset-password`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ newPassword: newPassword, token: resetToken }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || 'Đặt lại mật khẩu thất bại.');
      }

      if (data.success) {
        // Reset form and go back to login
        setOtp('');
        setNewPassword('');
        setPhone('');
        setResetToken('');
        setStep('login');
        setError('');
      } else {
        showError(data.message || 'Không thể đặt lại mật khẩu.');
      }
    } catch (error) {
      console.error('Reset password error:', error);
      showError(error.message || 'Không thể kết nối tới server.');
    } finally {
      setLoading(false);
    }
  };

  const resetForgotFlow = () => {
    setPhone('');
    setOtp('');
    setNewPassword('');
    setResetToken('');
    setStep('login');
    clearErrors();
  };
  return (
    <div className="wrapper">
      {step === 'login' && (
        <form onSubmit={handleLogin}>
          <h1>Login</h1>

          {error && <div className="error-message">{error}</div>}

          <div className="input-box">
            <input
              type="text"
              placeholder="Username"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
            />
            <i className="bx bx-user"></i>
          </div>

          <div className="input-box">
            <input
              type="password"
              placeholder="Password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={loading}
            />
            <i className="bx bx-lock"></i>
          </div>

          <div className="remember-forgot">
            <label>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                disabled={loading}
              />{' '}
              Remember Me
            </label>
            <button
              type="button"
              className="text-link"
              onClick={() => {
                setStep('forgot');
                clearErrors();
              }}
              disabled={loading}
            >
              Forgot Password?
            </button>
          </div>

          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
      )}

      {step === 'forgot' && (
        <form onSubmit={handleSendOtp}>
          <h1>Forgot Password</h1>

          {error && <div className="error-message">{error}</div>}
          {phoneError && <div className="error-message">{phoneError}</div>}

          <div className="input-box">
            <input
              type="text"
              placeholder="Nhập số điện thoại"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
              disabled={loading}
            />
            <i className="bx bx-phone"></i>
          </div>

          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Sending...' : 'Send OTP'}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={resetForgotFlow}
            disabled={loading}
          >
            Back to Login
          </button>
        </form>
      )}

      {step === 'otp' && (
        <form onSubmit={handleVerifyOtp}>
          <h1>Verify OTP</h1>

          {error && <div className="error-message">{error}</div>}
          {otpError && <div className="error-message">{otpError}</div>}

          <div className="input-box">
            <input
              type="text"
              placeholder="Enter 6-digit OTP"
              inputMode="numeric"
              maxLength={6}
              value={otp}
              onChange={(e) => {
                const value = e.target.value;
                if (/^\d{0,6}$/.test(value)) {
                  setOtp(value);
                }
              }}
              required
              disabled={loading}
            />
            <i className="bx bx-shield"></i>
          </div>

          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Verifying...' : 'Verify OTP'}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => setStep('forgot')}
            disabled={loading}
          >
            Back
          </button>
        </form>
      )}

      {step === 'reset' && (
        <form onSubmit={handleResetPassword}>
          <h1>Reset Password</h1>

          {error && <div className="error-message">{error}</div>}
          {passwordError && <div className="error-message">{passwordError}</div>}

          <div className="input-box">
            <input
              type="password"
              placeholder="New password (8+ chars, uppercase, lowercase, number)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              disabled={loading}
            />
            <i className="bx bx-lock-alt"></i>
          </div>

          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Resetting...' : 'Confirm'}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => setStep('otp')}
            disabled={loading}
          >
            Back
          </button>
        </form>
      )}
    </div>
  );
};

export default Login;