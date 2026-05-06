import axiosClient from "../../../api/axiosClient";

export const adminApi = {
  // Lấy danh sách cấu hình giá
  getPricingRules: () => {
    return axiosClient.get("/v1/admin/pricing-rules");
  },

  // Tạo cấu hình giá mới (Vé tháng - FLAT_RATE)
  createPricingRule: (data) => {
    return axiosClient.post("/v1/admin/pricing-rules", data);
  },

  // Cập nhật cấu hình
  updatePricingRule: (id, data) => {
    return axiosClient.put(`/v1/admin/pricing-rules/${id}`, data);
  },

  // Xóa cấu hình
  deletePricingRule: (id) => {
    return axiosClient.delete(`/v1/admin/pricing-rules/${id}`);
  },
};
