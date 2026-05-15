import axiosClient from "../../../api/axiosClient";

export const adminApi = {
  getPricingRules: () => axiosClient.get("/v1/admin/pricing-rules"),
  createPricingRule: (data) => axiosClient.post("/v1/admin/pricing-rules", data),
  updatePricingRule: (id, data) => axiosClient.put(`/v1/admin/pricing-rules/${id}`, data),
  deletePricingRule: (id) => axiosClient.delete(`/v1/admin/pricing-rules/${id}`),
  
  getUsers: (params) => axiosClient.get("/v1/admin/users", { params }),
  createUser: (data) => axiosClient.post("/v1/admin/users", data),
  updateUser: (id, data) => axiosClient.put(`/v1/admin/users/${id}`, data),
  deleteUser: (id) => axiosClient.delete(`/v1/admin/users/${id}`),
};