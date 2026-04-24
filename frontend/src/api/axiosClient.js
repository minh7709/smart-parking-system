import API_ENDPOINTS from './endpoints';

const API_BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/api';

const buildUrl = (path) => `${API_BASE_PATH}${path}`;

const cleanHeaders = (headers = {}) =>
  Object.fromEntries(Object.entries(headers).filter(([, value]) => value !== undefined));

let refreshPromise = null;

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

const buildAuthHeaders = () => {
  const token = normalizeAccessToken(localStorage.getItem('accessToken'));
  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
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

const shouldSkipRefresh = (path) =>
  path === API_ENDPOINTS.auth.login ||
  path === API_ENDPOINTS.auth.refresh ||
  path === API_ENDPOINTS.auth.forgotPassword ||
  path === API_ENDPOINTS.auth.verifyOtp ||
  path === API_ENDPOINTS.auth.resetPassword;

const tryRefreshToken = async () => {
  const refreshToken = normalizeAccessToken(localStorage.getItem('refreshToken'));

  if (!refreshToken) {
    clearAuthStorage();
    return false;
  }

  if (!refreshPromise) {
    refreshPromise = (async () => {
      const response = await fetch(buildUrl(API_ENDPOINTS.auth.refresh), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      const contentType = response.headers.get('content-type') || '';
      const isJson = contentType.includes('application/json');
      const payload = isJson ? await response.json() : null;

      if (!response.ok || !payload?.data?.accessToken) {
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

const executeRequest = (method, path, body, options = {}) => {
  const { headers: optHeaders, ...restOptions } = options;
  return fetch(buildUrl(path), {
    method,
    headers: cleanHeaders({
      ...buildAuthHeaders(),
      ...(optHeaders || {}),
    }),
    body,
    ...restOptions,
  });
};

const request = async (method, path, body, options = {}) => {
  const canRetryWithRefresh = options.retryOn401 !== false;
  let response = await executeRequest(method, path, body, options);

  if (response.status === 401 && canRetryWithRefresh && !shouldSkipRefresh(path)) {
    const refreshed = await tryRefreshToken();

    if (refreshed) {
      response = await executeRequest(method, path, body, {
        ...options,
        retryOn401: false,
      });
    }
  }

  return parseResponse(response);
};

const parseResponse = async (response) => {
  const contentType = response.headers.get('content-type') || '';
  const isJson = contentType.includes('application/json');
  const payload = isJson ? await response.json() : null;

  if (!response.ok) {
    const message = payload?.message || `Request failed with status ${response.status}`;
    const error = new Error(message);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload;
};

const axiosClient = {
  post: async (path, body, options = {}) => {
    return request('POST', path, JSON.stringify(body), {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
    });
  },
  get: async (path, options = {}) => request('GET', path, undefined, options),
  put: async (path, body, options = {}) =>
    request('PUT', path, JSON.stringify(body), {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
    }),
  delete: async (path, options = {}) => request('DELETE', path, undefined, options),
  postForm: async (path, formData, options = {}) =>
    request('POST', path, formData, {
      ...options,
      headers: {
        ...(options.headers || {}),
      },
    }),
};

export default axiosClient;
