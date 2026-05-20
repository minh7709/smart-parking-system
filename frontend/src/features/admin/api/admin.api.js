import axiosClient from "../../../api/axiosClient";

export const adminApi = {
  getPricingRules: () => axiosClient.get("/v1/admin/pricing-rules"),
  createPricingRule: (data) => axiosClient.post("/v1/admin/pricing-rules", data),
  updatePricingRule: (id, data) => axiosClient.put(`/v1/admin/pricing-rules/${id}`, data),
  deletePricingRule: (id) => axiosClient.delete(`/v1/admin/pricing-rules/${id}`),
  activatePricingRule: (id) => axiosClient.post(`/v1/admin/pricing-rules/${id}/activate`),
  deactivatePricingRule: (id) => axiosClient.post(`/v1/admin/pricing-rules/${id}/deactivate`),
  
  getUsers: (params) => axiosClient.get("/v1/admin/users", { params }),
  createUser: (data) => axiosClient.post("/v1/admin/users", data),
  updateUser: (id, data) => axiosClient.put(`/v1/admin/users/${id}`, data),
  deleteUser: (id) => axiosClient.delete(`/v1/admin/users/${id}`),

  getIncidents: (params) => axiosClient.get("/v1/admin/incidents", { params }),
  getIncidentEvidence: (incidentId) => axiosClient.get("/v1/admin/incidents/evidence", {
    params: { incidentId },
    responseType: "blob",
  }),
  getIncidentTypes: () => axiosClient.get("/v1/type/incident-types"),

  getLanes: () => axiosClient.get("/v1/admin/lanes"),
  createLane: (data) => axiosClient.post("/v1/admin/lanes", data),
  updateLane: (id, data) => axiosClient.put(`/v1/admin/lanes/${id}`, data),
  deleteLane: (id) => axiosClient.delete(`/v1/admin/lanes/${id}`),
  getLaneTypes: () => axiosClient.get("/v1/type/lane-types"),
  getLaneStatuses: () => axiosClient.get("/v1/type/lane-statuses"),
};