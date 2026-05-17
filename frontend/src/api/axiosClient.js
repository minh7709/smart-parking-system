import axios from 'axios';
import API_ENDPOINTS from './endpoints';

const API_BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/api';

const axiosClient = axios.create({
  baseURL: API_BASE_PATH,
  headers: {
    'Content-Type': 'application/json',
  },
});

const normalizeAccessToken = (rawToken) => {
  if (!rawToken) {
    return null;
  }

  let token = String(rawToken).trim();

  if (token.startsWith('"') && token.endsWith('"')) {
    token = token.slice(1, -1).trim();
  }

  token = token.replace(/^Bearer\s+/i, '').trim();

  return token || null;
};

const clearAuthStorage = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('tokenType');
  localStorage.removeItem('expiresIn');
  localStorage.removeItem('expiresAt');
  localStorage.removeItem('user');
};

const persistRefreshData = (data) => {
  if (!data?.accessToken) {
    return;
  }

  localStorage.setItem('accessToken', data.accessToken);

  if (data.refreshToken) {
    localStorage.setItem('refreshToken', data.refreshToken);
  }

  if (data.tokenType) {
    localStorage.setItem('tokenType', data.tokenType);
  }

  if (typeof data.expiresIn === 'number') {
    const expiresAt = Date.now() + data.expiresIn * 1000;
    localStorage.setItem('expiresIn', String(data.expiresIn));
    localStorage.setItem('expiresAt', String(expiresAt));
  }
};

const shouldSkipRefresh = (url) => {
  if (!url) return true;
  return url.includes(API_ENDPOINTS.auth.login) ||
    url.includes(API_ENDPOINTS.auth.refresh) ||
    url.includes(API_ENDPOINTS.auth.forgotPassword) ||
    url.includes(API_ENDPOINTS.auth.verifyOtp) ||
    url.includes(API_ENDPOINTS.auth.resetPassword);
};

let refreshPromise = null;

const tryRefreshToken = async () => {
  const refreshToken = normalizeAccessToken(localStorage.getItem('refreshToken'));

  if (!refreshToken) {
    clearAuthStorage();
    return false;
  }

  if (!refreshPromise) {
    refreshPromise = (async () => {
      // Using vanilla axios for the refresh token request to avoid interceptors
      const response = await axios.post(`${API_BASE_PATH}${API_ENDPOINTS.auth.refresh}`, { refreshToken }, {
        headers: { 'Content-Type': 'application/json' }
      });

      const payload = response.data;
      if (!payload?.data?.accessToken) {
        throw new Error(payload?.message || 'Refresh token failed');
      }

      persistRefreshData(payload.data);
      return true;
    })()
      .catch(() => {
        clearAuthStorage();
        return false;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
};

// Request Interceptor
axiosClient.interceptors.request.use(
  (config) => {
    const token = normalizeAccessToken(localStorage.getItem('accessToken'));
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
axiosClient.interceptors.response.use(
  (response) => {
    // Return data directly to match old behavior
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // Check if the error is 401 and we should retry
    if (
      error.response?.status === 401 && 
      !originalRequest._retry && 
      originalRequest.retryOn401 !== false && 
      !shouldSkipRefresh(originalRequest.url)
    ) {
      originalRequest._retry = true;
      const refreshed = await tryRefreshToken();
      if (refreshed) {
        // Return custom axiosClient instance to retry request
        return axiosClient(originalRequest);
      }
    }

    // Parse error to match previous behavior
    const payload = error.response?.data || null;
    const message = payload?.message || error.message || `Request failed with status ${error.response?.status}`;
    const customError = new Error(message);
    customError.status = error.response?.status;
    customError.payload = payload;

    return Promise.reject(customError);
  }
);

export default axiosClient;
