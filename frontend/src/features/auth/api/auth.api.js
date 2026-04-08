import axiosClient from '../../../api/axiosClient';
import API_ENDPOINTS from '../../../api/endpoints';

export const loginApi = (payload) => axiosClient.post(API_ENDPOINTS.auth.login, payload);

export const refreshTokenApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.auth.refresh, payload);

export const logoutApi = (payload, accessToken) =>
  axiosClient.post(API_ENDPOINTS.auth.logout, payload, {
    headers: {
      Authorization: accessToken ? `Bearer ${accessToken}` : undefined,
    },
  });

export const getCurrentUserApi = () => axiosClient.get(API_ENDPOINTS.auth.me);

export const forgotPasswordApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.auth.forgotPassword, payload);

export const verifyOtpApi = (payload) => axiosClient.post(API_ENDPOINTS.auth.verifyOtp, payload);

export const resetPasswordApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.auth.resetPassword, payload);
