import axiosClient from "../../../api/axiosClient";

export const adminApi = {
  // Lấy danh sách cấu hình giá
  getPricingRules: () => {
    return axiosClient.get("/api/pricing-rules"); // Thay đổi URL cho khớp với backend của bạn
  },

  // Tạo cấu hình giá mới (Vé tháng - FLAT_RATE)
  createPricingRule: (data) => {
    return axiosClient.post("/api/pricing-rules", data);
  },

  // Cập nhật cấu hình
  updatePricingRule: (id, data) => {
    return axiosClient.put(`/api/pricing-rules/${id}`, data);
  },

  // Xóa cấu hình
  deletePricingRule: (id) => {
    return axiosClient.delete(`/api/pricing-rules/${id}`);
  },
};
