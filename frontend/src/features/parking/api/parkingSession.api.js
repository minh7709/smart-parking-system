import axiosClient from '../../../api/axiosClient';
import API_ENDPOINTS from '../../../api/endpoints';

export const confirmCheckInApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.guard.parkingSession.confirmCheckIn, payload);

export const confirmCheckOutApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.guard.parkingSession.confirmCheckOut, payload);

export const checkInApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.checkIn, formData, options);

export const checkOutApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.checkOut, formData, options);