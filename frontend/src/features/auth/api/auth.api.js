import axiosClient from '../../../api/axiosClient';

export const loginApi = (payload) => axiosClient.post('/v1/auth/login', payload);

export const forgotPasswordApi = (payload) =>
  axiosClient.post('/v1/auth/forgot-password', payload);

export const verifyOtpApi = (payload) => axiosClient.post('/v1/auth/verify-otp', payload);

export const resetPasswordApi = (payload) =>
  axiosClient.post('/v1/auth/reset-password', payload);
