// import React, { useState } from 'react';
// import './Login.css';

// const Login = () => {
//   const [step, setStep] = useState('login');
//   const [username, setUsername] = useState('');
//   const [password, setPassword] = useState('');
//   const [rememberMe, setRememberMe] = useState(false);
//   const [phone, setPhone] = useState('');
//   const [otp, setOtp] = useState('');
//   const [newPassword, setNewPassword] = useState('');

//   const saveAuthToLocalStorage = (authData) => {
//     const expiresAt = Date.now() + authData.expiresIn * 1000;

//     localStorage.setItem('accessToken', authData.accessToken);
//     localStorage.setItem('refreshToken', authData.refreshToken);
//     localStorage.setItem('tokenType', authData.tokenType);
//     localStorage.setItem('expiresIn', String(authData.expiresIn));
//     localStorage.setItem('expiresAt', String(expiresAt));
//     localStorage.setItem('user', JSON.stringify(authData.user));
//   };

//   const handleLogin = async (e) => {
//     e.preventDefault();

//     const loginData = {
//       username: username,
//       password: password,
//       rememberMe: rememberMe
//     };

//     try {
//       const response = await fetch('/api/v1/auth/login', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify(loginData),
//       });

//       const data = await response.json();
//       console.log('Response from server:', data);
//       console.log('HTTP status code:', response);
//       if (!response.ok) {
//         alert(data.message || 'Đăng nhập thất bại.');
//         return;
//       }

//       if (data.success && data.data?.accessToken && data.data?.refreshToken) {
//         saveAuthToLocalStorage(data.data);
//         alert('Đăng nhập thành công!');
//       } else {
//         alert(data.message || 'Dữ liệu đăng nhập không hợp lệ.');
//       }
//     } catch (error) {
//       console.error('Lỗi kết nối đến server Spring Boot:', error);
//       alert('Không thể kết nối tới server. Hãy kiểm tra backend và khởi động lại Vite dev server.');
//     }
//   };

//   const handleSendOtp = (e) => {
//     e.preventDefault();

//     if (!phone.trim()) {
//       alert('Vui lòng nhập số điện thoại.');
//        try {
//       const response = await fetch('/api/v1/auth/forgot-password', {
//         method: 'POST',
//         headers: {
//           'Content-Type': 'application/json',
//         },
//         body: JSON.stringify({ phone: phone }),
//       });

//       const data = await response.json();
//       console.log('Response from server:', data);
//       console.log('HTTP status code:', response);
//       if (!response.ok) {
//         alert(data.message || 'Đăng nhập thất bại.');
//         return;
//       }
//       if (data.success && data.data?.accessToken && data.data?.refreshToken) {
//         saveAuthToLocalStorage(data.data);
//         alert('Đăng nhập thành công!');
//       } else {        alert(data.message || 'Dữ liệu đăng nhập không hợp lệ.');
//       } catch (error) {
//       console.error('Lỗi kết nối đến server Spring Boot:', error);
//       alert('Không thể kết nối tới server. Hãy kiểm tra backend và khởi động lại Vite dev server.');
//     }
//       return;
//     }

//     alert('Đã gửi OTP (demo).');
//     setStep('otp');
//   };

//   const handleVerifyOtp = (e) => {
//     e.preventDefault();

//     if (!/^\d{6}$/.test(otp)) {
//       alert('OTP phải gồm đúng 6 chữ số.');
//       return;
//     }

//     setStep('reset');
//   };

//   const handleResetPassword = (e) => {
//     e.preventDefault();

//     if (newPassword.trim().length < 6) {
//       alert('Mật khẩu mới tối thiểu 6 ký tự.');
//       return;
//     }

//     alert('Đặt lại mật khẩu thành công (demo).');
//     setOtp('');
//     setNewPassword('');
//     setStep('login');
//   };

//   const resetForgotFlow = () => {
//     setPhone('');
//     setOtp('');
//     setNewPassword('');
//     setStep('login');
//   };
//   return (
//     <div className="wrapper">
//       {step === 'login' && (
//         <form onSubmit={handleLogin}>
//           <h1>Login</h1>

//           <div className="input-box">
//             <input
//               type="text"
//               placeholder="Username"
//               required
//               value={username}
//               onChange={(e) => setUsername(e.target.value)}
//             />
//             <i className="bx bx-user"></i>
//           </div>

//           <div className="input-box">
//             <input
//               type="password"
//               placeholder="Password"
//               required
//               value={password}
//               onChange={(e) => setPassword(e.target.value)}
//             />
//             <i className="bx bx-lock"></i>
//           </div>

//           <div className="remember-forgot">
//             <label>
//               <input
//                 type="checkbox"
//                 checked={rememberMe}
//                 onChange={(e) => setRememberMe(e.target.checked)}
//               />{' '}
//               Remember Me
//             </label>
//             <button
//               type="button"
//               className="text-link"
//               onClick={() => setStep('forgot')}
//             >
//               Forgot Password?
//             </button>
//           </div>

//           <button type="submit" className="btn">Login</button>
//         </form>
//       )}

//       {step === 'forgot' && (
//         <form onSubmit={handleSendOtp}>
//           <h1>Forgot Password</h1>

//           <div className="input-box">
//             <input
//               type="text"
//               placeholder="Phone number"
//               value={phone}
//               onChange={(e) => setPhone(e.target.value)}
//               required
//             />
//             <i className="bx bx-phone"></i>
//           </div>

//           <button type="submit" className="btn">Send OTP</button>
//           <button type="button" className="btn btn-secondary" onClick={resetForgotFlow}>
//             Back to Login
//           </button>
//         </form>
//       )}

//       {step === 'otp' && (
//         <form onSubmit={handleVerifyOtp}>
//           <h1>Verify OTP</h1>

//           <div className="input-box">
//             <input
//               type="text"
//               placeholder="Enter 6-digit OTP"
//               inputMode="numeric"
//               maxLength={6}
//               value={otp}
//               onChange={(e) => {
//                 const value = e.target.value;
//                 if (/^\d{0,6}$/.test(value)) {
//                   setOtp(value);
//                 }
//               }}
//               required
//             />
//             <i className="bx bx-shield"></i>
//           </div>

//           <button type="submit" className="btn">Verify OTP</button>
//           <button type="button" className="btn btn-secondary" onClick={() => setStep('forgot')}>
//             Back
//           </button>
//         </form>
//       )}

//       {step === 'reset' && (
//         <form onSubmit={handleResetPassword}>
//           <h1>Reset Password</h1>

//           <div className="input-box">
//             <input
//               type="password"
//               placeholder="New password"
//               value={newPassword}
//               onChange={(e) => setNewPassword(e.target.value)}
//               required
//             />
//             <i className="bx bx-lock-alt"></i>
//           </div>

//           <button type="submit" className="btn">Confirm</button>
//           <button type="button" className="btn btn-secondary" onClick={() => setStep('otp')}>
//             Back
//           </button>
//         </form>
//       )}
//     </div>
//   );
// };

// export default Login;