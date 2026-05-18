import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  forgotPasswordApi,
  loginApi,
  resetPasswordApi,
  verifyOtpApi,
} from "../api/auth.api";
import ForgotPasswordForm from "../components/ForgotPasswordForm";
import LoginForm from "../components/LoginForm";
import ResetPasswordForm from "../components/ResetPasswordForm";
import VerifyOtpForm from "../components/VerifyOtpForm";
import { saveAuthToLocalStorage, fetchAllSystemTypesApi, saveSystemTypes } from "../../../utils/storage";

import {
  validateOtp,
  validatePassword,
  validatePhone,
} from "../../../utils/validators";
import styles from "./LoginPage.module.css";
import { useNotification } from "../../../hooks/useNotification";

const LoginPage = () => {
  const navigate = useNavigate();
  const notify = useNotification();
  const [step, setStep] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [phone, setPhone] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [resetToken, setResetToken] = useState("");

  const [loading, setLoading] = useState(false);

  const resetForgotFlow = () => {
    setPhone("");
    setOtp("");
    setNewPassword("");
    setResetToken("");
    setStep("login");
  };

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!username.trim() || !password.trim()) {
      notify.error("Vui lòng nhập username và password");
      return;
    }

    setLoading(true);

    try {
      const response = await loginApi({
        username,
        password,
        rememberMe,
      });

      if (
        response.success &&
        response.data?.accessToken &&
        response.data?.refreshToken
      ) {
        saveAuthToLocalStorage(response.data);
        notify.success("Đăng nhập thành công!");
        const typesData = await fetchAllSystemTypesApi();
        saveSystemTypes(typesData);
        if (response.data.user.role === "ADMIN") {
          navigate("/admin/dashboard");
        } else {
          navigate("/lane");
        }
      } else {
        notify.error(response.message || "Dữ liệu đăng nhập không hợp lệ.");
      }
    } catch (err) {
      console.error("Login error:", err);
      notify.apiError(err, "Không thể kết nối tới server. Hãy kiểm tra backend.");
    } finally {
      setLoading(false);
    }
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();

    if (!phone.trim()) {
      notify.error("Vui lòng nhập số điện thoại");
      return;
    }

    if (!validatePhone(phone)) {
      notify.error("Số điện thoại không hợp lệ (10-11 chữ số)");
      return;
    }

    setLoading(true);

    try {
      const response = await forgotPasswordApi({ phone });

      if (response.success) {
        setStep("otp");
        notify.success("Đã gửi OTP thành công!");
      } else {
        notify.error(response.message || "Không thể gửi OTP.");
      }
    } catch (err) {
      console.error("Send OTP error:", err);
      notify.apiError(err, "Không thể kết nối tới server.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();

    if (!validateOtp(otp)) {
      notify.error("OTP phải gồm đúng 6 chữ số.");
      return;
    }

    setLoading(true);

    try {
      const response = await verifyOtpApi({ phone, otp });

      if (response.success && response.data) {
        setResetToken(response.data);
        setStep("reset");
        notify.success("Xác minh OTP thành công!");
      } else {
        notify.error(response.message || "OTP không hợp lệ.", "otp");
      }
    } catch (err) {
      console.error("Verify OTP error:", err);
      notify.apiError(err, "Không thể xác minh OTP. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();

    if (!newPassword.trim()) {
      notify.error("Vui lòng nhập mật khẩu mới.");
      return;
    }

    if (!validatePassword(newPassword)) {
      notify.error(
        "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số."
      );
      return;
    }

    if (!resetToken) {
      notify.error("Token không hợp lệ. Vui lòng thử lại từ bước xác minh OTP.");
      return;
    }

    setLoading(true);

    try {
      const response = await resetPasswordApi({
        newPassword,
        token: resetToken,
      });

      if (response.success) {
        setOtp("");
        setNewPassword("");
        setResetToken("");
        setStep("login");
        notify.success("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
      } else {
        notify.error(response.message || "Không thể đổi mật khẩu.");
      }
    } catch (err) {
      console.error("Reset password error:", err);
      notify.apiError(err, "Đã có lỗi xảy ra khi đổi mật khẩu.");
    } finally {
      setLoading(false);
    }
  };

  const renderStep = () => {
    switch (step) {
      case "login":
        return (
          <LoginForm
            username={username}
            password={password}
            rememberMe={rememberMe}
            loading={loading}
            onUsernameChange={setUsername}
            onPasswordChange={setPassword}
            onRememberMeChange={setRememberMe}
            onSubmit={handleLogin}
            onForgotPassword={() => {
              setStep("forgot");
            }}
          />
        );
      case "forgot":
        return (
          <ForgotPasswordForm
            phone={phone}
            loading={loading}
            onPhoneChange={setPhone}
            onSubmit={handleSendOtp}
            onBack={resetForgotFlow}
          />
        );
      case "otp":
        return (
          <VerifyOtpForm
            otp={otp}
            loading={loading}
            onOtpChange={setOtp}
            onSubmit={handleVerifyOtp}
            onBack={() => setStep("forgot")}
          />
        );
      case "reset":
        return (
          <ResetPasswordForm
            newPassword={newPassword}
            loading={loading}
            onNewPasswordChange={setNewPassword}
            onSubmit={handleResetPassword}
            onBack={() => setStep("otp")}
          />
        );
    }
  };

  return (
    <div className={styles.loginPage}>
      <div className={styles.wrapper}>
        {renderStep()}
      </div>
    </div>
  );
};

export default LoginPage;
