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

export const saveLaneSelection = ({ checkInLane, checkOutLane }) => {
  localStorage.setItem('selectedCheckInLane', JSON.stringify(checkInLane));
  localStorage.setItem('selectedCheckOutLane', JSON.stringify(checkOutLane));
};

export const getLaneSelection = () => {
  const checkInRaw = localStorage.getItem('selectedCheckInLane');
  const checkOutRaw = localStorage.getItem('selectedCheckOutLane');

  return {
    checkInLane: checkInRaw ? JSON.parse(checkInRaw) : null,
    checkOutLane: checkOutRaw ? JSON.parse(checkOutRaw) : null,
  };
};

export const clearLaneSelection = () => {
  localStorage.removeItem('selectedCheckInLane');
  localStorage.removeItem('selectedCheckOutLane');
};

export const saveActiveParkingSessionId = (parkingSessionId) => {
  if (!parkingSessionId) {
    return;
  }
  localStorage.setItem('activeParkingSessionId', String(parkingSessionId));
};

export const getActiveParkingSessionId = () =>
  localStorage.getItem('activeParkingSessionId');

export const clearActiveParkingSessionId = () => {
  localStorage.removeItem('activeParkingSessionId');
};
