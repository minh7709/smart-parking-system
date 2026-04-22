export const validatePhone = (phone) => {
  const phoneRegex = /^\+?[0-9]{10,11}$/;
  return phoneRegex.test(phone);
};

export const validatePassword = (password) => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
  return passwordRegex.test(password);
};

export const validateOtp = (otp) => /^\d{6}$/.test(otp);
