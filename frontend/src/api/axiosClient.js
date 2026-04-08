const API_BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/api';

const buildUrl = (path) => `${API_BASE_PATH}${path}`;

const cleanHeaders = (headers = {}) =>
  Object.fromEntries(Object.entries(headers).filter(([, value]) => value !== undefined));

const buildAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
};

const request = async (method, path, body, options = {}) => {
  const response = await fetch(buildUrl(path), {
    method,
    headers: cleanHeaders({
      ...buildAuthHeaders(),
      ...(options.headers || {}),
    }),
    body,
    ...options,
  });

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
