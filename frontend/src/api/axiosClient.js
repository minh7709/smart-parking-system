const API_BASE_PATH = import.meta.env.VITE_API_BASE_PATH || '/api';

const buildUrl = (path) => `${API_BASE_PATH}${path}`;

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
    const response = await fetch(buildUrl(path), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
      body: JSON.stringify(body),
      ...options,
    });

    return parseResponse(response);
  },
};

export default axiosClient;
