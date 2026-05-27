import { render, screen, waitFor } from "@testing-library/react";
import VehiclePage from "../features/parking/pages/VehiclePage";
import * as vehicleApi from "../features/parking/api/vehicleApi";

jest.mock("../features/parking/api/vehicleApi", () => ({
  getVehiclesApi: jest.fn(),
  createVehicleApi: jest.fn(),
  updateVehicleApi: jest.fn(),
  deleteVehicleApi: jest.fn(),
}));

jest.mock("../hooks/useNotification", () => ({
  useNotification: () => ({
    success: jest.fn(),
    error: jest.fn(),
    apiError: jest.fn(),
    warning: jest.fn(),
  }),
}));

jest.mock("../utils/storage", () => ({
  getSystemTypes: () => [],
}));

describe("VehiclePage", () => {
  test("load danh sach phuong tien", async () => {
    vehicleApi.getVehiclesApi.mockResolvedValue({
      data: { content: [], totalElements: 0 },
    });

    render(<VehiclePage />);

    await waitFor(() => {
      expect(screen.getByText("Tạo mới")).toBeInTheDocument();
    });
  });
});
