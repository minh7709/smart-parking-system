import axiosClient from '../../../api/axiosClient';
import API_ENDPOINTS from '../../../api/endpoints';

export const confirmCheckInApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.guard.parkingSession.confirmCheckIn, payload);

export const confirmCheckOutApi = (payload) =>
  axiosClient.post(API_ENDPOINTS.guard.parkingSession.confirmCheckOut, payload);

export const checkInApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.checkIn, formData, options);

export const cancelCheckInApi = (imageUrl, options = {}) =>
  axiosClient.delete(API_ENDPOINTS.guard.parkingSession.cancelCheckIn, {
    data: imageUrl,
    ...options,
  });

export const checkOutApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.checkOut, formData, options);

export const reportIncidentApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.reportIncident, formData, options);

export const reportLostCardApi = (formData, options = {}) =>
  axiosClient.postForm(API_ENDPOINTS.guard.parkingSession.reportLostCard, formData, options);

export const getParkingSessionsApi = (params = {}) =>
  axiosClient.get(API_ENDPOINTS.guard.parkingSession.base, { params });

export const getParkingSessionsByLicensePlateApi = (licensePlate, params = {}) =>
  axiosClient.get(API_ENDPOINTS.guard.parkingSession.byLicensePlate(licensePlate), { params });

export const countParkingSessionsApi = (params = {}) =>
  axiosClient.get(API_ENDPOINTS.guard.parkingSession.countParking, { params });

export const getParkingSessionImageApi = (parkingSessionId, type, options = {}) =>
  axiosClient.get(API_ENDPOINTS.guard.parkingSession.imageById(parkingSessionId, type), {
    responseType: 'blob',
    ...options,
  });

export const getParkingSessionImageByUrlApi = (imageUrl, options = {}) =>
  axiosClient.get(API_ENDPOINTS.guard.parkingSession.imageByUrl(imageUrl), {
    responseType: 'blob',
    ...options,
  });