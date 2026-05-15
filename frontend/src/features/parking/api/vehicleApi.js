import axiosClient from '../../../api/axiosClient';
import API_ENDPOINTS from '../../../api/endpoints';

export const getVehiclesApi = (params) => {
  return axiosClient.get(API_ENDPOINTS.vehicles.base, { params });
};

export const getVehicleByLicensePlateApi = (plate) => {
  return axiosClient.get(API_ENDPOINTS.vehicles.byLicensePlate(plate));
};

export const createVehicleApi = (data) => {
  return axiosClient.post(API_ENDPOINTS.vehicles.base, data);
};

export const updateVehicleApi = (id, data) => {
  return axiosClient.put(API_ENDPOINTS.vehicles.byId(id), data);
};

export const deleteVehicleApi = (id) => {
  return axiosClient.delete(API_ENDPOINTS.vehicles.byId(id));
};
