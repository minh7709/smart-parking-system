import axiosClient from "../../../api/axiosClient";
import API_ENDPOINTS from "../../../api/endpoints";

export const getSubscriptionsApi = async (params = {}) => {
  return await axiosClient.get(API_ENDPOINTS.guard.subscriptions.base, { params });
};

export const createSubscriptionApi = async (data) => {
  return await axiosClient.post(API_ENDPOINTS.guard.subscriptions.base, data);
};

export const updateSubscriptionApi = async (id, data) => {
  return await axiosClient.put(API_ENDPOINTS.guard.subscriptions.byId(id), data);
};

export const deleteSubscriptionApi = async (id) => {
  return await axiosClient.delete(API_ENDPOINTS.guard.subscriptions.byId(id));
};

export const getSubscriptionByIdApi = async (id) => {
  return await axiosClient.get(API_ENDPOINTS.guard.subscriptions.byId(id));
};

export const getSubscriptionByLicensePlateApi = async (licensePlate) => {
  return await axiosClient.get(API_ENDPOINTS.guard.subscriptions.byLicensePlate(licensePlate));
};
