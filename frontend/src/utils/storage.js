export const saveAuthToLocalStorage = (authData) => {
  const expiresAt = Date.now() + authData.expiresIn * 1000;

  localStorage.setItem('accessToken', authData.accessToken);
  localStorage.setItem('refreshToken', authData.refreshToken);
  localStorage.setItem('tokenType', authData.tokenType);
  localStorage.setItem('expiresIn', String(authData.expiresIn));
  localStorage.setItem('expiresAt', String(expiresAt));
  localStorage.setItem('user', JSON.stringify(authData.user));
};

export const getAccessToken = () => localStorage.getItem('accessToken');

export const clearAuthFromLocalStorage = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('tokenType');
  localStorage.removeItem('expiresIn');
  localStorage.removeItem('expiresAt');
  localStorage.removeItem('user');
};

export const saveResetPasswordToken = (token) => {
    localStorage.setItem('resetPasswordToken', token);
};
