import { render, screen, waitFor } from "@testing-library/react";
import AdminLane from "../features/admin/pages/AdminLane";
import { adminApi } from "../features/admin/api/admin.api";

jest.mock("../features/admin/api/admin.api", () => ({
  adminApi: {
    getLanes: jest.fn(),
    deleteLane: jest.fn(),
    updateLane: jest.fn(),
    createLane: jest.fn(),
  },
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

describe("AdminLane", () => {
  test("load danh sach lan", async () => {
    adminApi.getLanes.mockResolvedValue({
      data: [
        {
          id: "1",
          laneName: "Lane A",
          laneType: { value: "IN", label: "IN" },
          status: { value: "ACTIVE", label: "ACTIVE" },
          ipCamera: "192.168.1.10",
        },
      ],
    });

    render(<AdminLane />);

    await waitFor(() => {
      expect(screen.getByText("Lane A")).toBeInTheDocument();
    });
  });
});
